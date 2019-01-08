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
    public List<Medicamento> busquedaForm(String name,String prinActivo,String fabricante){
        String toret="SELECT m FROM Medicamento m WHERE ";
        boolean unico=true;
        if(!name.isEmpty()){
            toret+="m.nombre LIKE :name";
            unico=false;
        }
        if(!prinActivo.isEmpty()){
            if(!unico) toret+=" AND ";
            else unico=false;
            toret+="m.principioActivo LIKE :principio";
        }
        if(!fabricante.isEmpty()){
            if(!unico) toret+=" AND ";
            toret+="m.fabricante LIKE :fabricante";
        }
        TypedQuery<Medicamento> q = em.createQuery(toret,Medicamento.class);
        q.setParameter("name",name);
        q.setParameter("principio",prinActivo);
        q.setParameter("fabricante",fabricante);
        return q.getResultList();
    }
}
