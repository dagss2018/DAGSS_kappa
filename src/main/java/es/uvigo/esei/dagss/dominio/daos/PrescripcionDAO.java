/*
 Proyecto Java EE, DAGSS-2016
 */
package es.uvigo.esei.dagss.dominio.daos;
import es.uvigo.esei.dagss.dominio.entidades.EstadoReceta;
import es.uvigo.esei.dagss.dominio.entidades.Prescripcion;
import es.uvigo.esei.dagss.dominio.entidades.Receta;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

@Stateless
@LocalBean
public class PrescripcionDAO extends GenericoDAO<Prescripcion> {
    
    @EJB
    private RecetaDAO recetaDAO;

    public Prescripcion buscarPorIdConRecetas(Long id) {
        TypedQuery<Prescripcion> q = em.createQuery("SELECT p FROM Prescripcion AS p JOIN FETCH p.recetas"
                                                  + "  WHERE p.id = :id", Prescripcion.class);
        q.setParameter("id", id);
        
        return q.getSingleResult();
    }
    
    // Completar aqui
    @Override
    public Prescripcion crear(Prescripcion p){ //se le pasa presquipcion con recetas=null
        long time=p.getFechaFin().getTime()-p.getFechaInicio().getTime();
        final long quinceDias=1296000000; //quince dias en ms
        Date fechaInicio=new Date();
        int numReceta=0;
        ArrayList<Receta> recetas=new ArrayList<>();
        do{
            fechaInicio.setTime(p.getFechaInicio().getTime()+quinceDias*numReceta);
            numReceta++;
            Receta r=this.recetaDAO.crear(new Receta(p,p.getDosis(),fechaInicio,p.getFechaFin(),EstadoReceta.GENERADA,null));
            recetas.add(r);
            time-=quinceDias;
        }while(time>=0);
        p.setRecetas(recetas);
        this.em.persist(p); // Crea una nueva tupla en la BD
        return p;
    }
    
    public List<Prescripcion> buscarPorIdPaciente(String idPaciente){
        TypedQuery<Prescripcion> q = em.createQuery("SELECT p FROM Prescripcion p WHERE p.paciente.id=:id", Prescripcion.class);
        q.setParameter("id",idPaciente);
        return q.getResultList();
    }
}