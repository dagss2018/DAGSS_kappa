/*
 Proyecto Java EE, DAGSS-2014
 */

package es.uvigo.esei.dagss.dominio.daos;

import es.uvigo.esei.dagss.dominio.entidades.Cita;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;


@Stateless
@LocalBean
public class CitaDAO  extends GenericoDAO<Cita>{    
    // Completar aqui
    public List<Cita> buscarPorNSS(String nss) {
        TypedQuery<Cita> q = em.createQuery("SELECT c FROM Cita c WHERE c.paciente.numeroSeguridadSocial=:nss", Cita.class);
        q.setParameter("nss",nss);
        return q.getResultList();
    }
    
    public List<Cita> buscarPorFecha(String fecha){
        TypedQuery<Cita> q = em.createQuery("SELECT c FROM Cita c WHERE c.fecha=:fecha", Cita.class);
        q.setParameter("fecha",fecha);
        return q.getResultList();
    }
    
    public List<Cita> buscarPorEstado(String est){
        TypedQuery<Cita> q = em.createQuery("SELECT c FROM Cita c WHERE c.estado=:est", Cita.class);
        q.setParameter("est",est);
        return q.getResultList();
    }
    
    public List<Cita> buscarPorDniMedico(String dniMedico){
        TypedQuery<Cita> q = em.createQuery("SELECT c FROM Cita c WHERE c.medico.dni=:dni", Cita.class);
        q.setParameter("dni",dniMedico);
        return q.getResultList();
    }
}