/*
 Proyecto Java EE, DAGSS-2013
 */
package es.uvigo.esei.dagss.controladores.farmacia;

import es.uvigo.esei.dagss.controladores.autenticacion.AutenticacionControlador;
import es.uvigo.esei.dagss.dominio.daos.FarmaciaDAO;
import es.uvigo.esei.dagss.dominio.daos.RecetaDAO;
import es.uvigo.esei.dagss.dominio.entidades.Direccion;
import es.uvigo.esei.dagss.dominio.entidades.EstadoReceta;
import es.uvigo.esei.dagss.dominio.entidades.Farmacia;
import es.uvigo.esei.dagss.dominio.entidades.Receta;
import es.uvigo.esei.dagss.dominio.entidades.TipoUsuario;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author ribadas
 */
@Named(value = "farmaciaControlador")
@SessionScoped
public class FarmaciaControlador implements Serializable {

    private Farmacia farmaciaActual;
    private String nif;
    private String password;

    @Inject
    private AutenticacionControlador autenticacionControlador;

    @EJB
    private FarmaciaDAO farmaciaDAO;
    
    @EJB
    private RecetaDAO recetaDAO;

    /**
     * Creates a new instance of AdministradorControlador
     */
    public FarmaciaControlador() {
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Farmacia getFarmaciaActual() {
        return farmaciaActual;
    }

    public void setFarmaciaActual(Farmacia farmaciaActual) {
        this.farmaciaActual = farmaciaActual;
    }

    private boolean parametrosAccesoInvalidos() {
        return ((nif == null) || (password == null));
    }

    public String doLogin() {
        String destino = null;
        if (parametrosAccesoInvalidos()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No se ha indicado un nif o una contraseña", ""));
        } else {
            Farmacia farmacia = farmaciaDAO.buscarPorNIF(nif);
            if (farmacia == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe una farmacia con el NIF " + nif, ""));
            } else {
                if (autenticacionControlador.autenticarUsuario(farmacia.getId(), password,
                        TipoUsuario.FARMACIA.getEtiqueta().toLowerCase())) {
                    farmaciaActual = farmacia;
                    destino = "privado/index";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }

            }
        }
        return destino;
    }
    
    //modifica los datos de la farmacia logeada
    public void cambiarDatosFarmaciaActual(String pass,String nombre,Direccion dir){
        if(this.farmaciaActual!=null){
            if(pass!=null){
                this.setPassword(pass);
                this.farmaciaActual.setPassword(pass);
            }
            if(nombre!=null) this.farmaciaActual.setNombreFarmacia(nombre);
            if(dir!=null) this.farmaciaActual.setDireccion(dir);
            this.farmaciaDAO.actualizar(this.farmaciaActual);
        }
    }
    
    //devuelve las lista de recetas actuales de un paciente (null si la farmacia no esta logueada)
    public List<Receta> getRecetasPaciente(String numTarjSan){
        if(this.autenticacionControlador.isUsuarioAutenticado()){
            List<Receta> toret=this.recetaDAO.buscarPorNumTarjeta(numTarjSan);
            for(Receta receta:toret) if(!receta.enFecha()) toret.remove(receta);
            return toret;
        }
        else return null;
    }
    
    //cambia el estado de las recetas generadas a servidas
    public void setRecetasServidas(List<Receta> recetas){
        for(Receta receta:recetas){
            if(receta.enFecha() && receta.getEstado().equals(EstadoReceta.GENERADA)){
                receta.setEstado(EstadoReceta.SERVIDA);
                this.recetaDAO.actualizar(receta);
            }
        }
    }
}
