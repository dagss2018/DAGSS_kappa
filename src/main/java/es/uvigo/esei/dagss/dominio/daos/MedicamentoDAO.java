/*
 Proyecto Java EE, DAGSS-2014
 */
package es.uvigo.esei.dagss.dominio.daos;

import es.uvigo.esei.dagss.dominio.entidades.Medicamento;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

@Stateless
@LocalBean
public class MedicamentoDAO extends GenericoDAO<Medicamento> {

    // Completar aqui
    public List<Medicamento> busquedaPorNombre(String name){
        TypedQuery<Medicamento> q = em.createQuery("SELECT m FROM Medicamento m WHERE m.nombre LIKE :name", Medicamento.class);
        q.setParameter("name",name);
        return q.getResultList();
    }
    
    public List<Medicamento> busquedaPorPrinActivo(String prinActivo){
        TypedQuery<Medicamento> q = em.createQuery("SELECT m FROM Medicamento m WHERE m.principioActivo LIKE :prinActivo", Medicamento.class);
        q.setParameter("prinActivo",prinActivo);
        return q.getResultList();
    }
    
    public List<Medicamento> busquedaPorFabricante(String fabricante){
        TypedQuery<Medicamento> q = em.createQuery("SELECT m FROM Medicamento m WHERE m.fabricante LIKE :fabricante", Medicamento.class);
        q.setParameter("fabricante",fabricante);
        return q.getResultList();
    }
}
