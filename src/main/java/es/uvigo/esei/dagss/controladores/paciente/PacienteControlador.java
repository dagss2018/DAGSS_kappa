/*
 Proyecto Java EE, DAGSS-2013
 */
package es.uvigo.esei.dagss.controladores.paciente;

import es.uvigo.esei.dagss.controladores.autenticacion.AutenticacionControlador;
import es.uvigo.esei.dagss.dominio.daos.CitaDAO;
import es.uvigo.esei.dagss.dominio.daos.PacienteDAO;
import es.uvigo.esei.dagss.dominio.daos.RecetaDAO;
import es.uvigo.esei.dagss.dominio.entidades.Cita;
import es.uvigo.esei.dagss.dominio.entidades.Direccion;
import es.uvigo.esei.dagss.dominio.entidades.EstadoCita;
import es.uvigo.esei.dagss.dominio.entidades.EstadoReceta;
import es.uvigo.esei.dagss.dominio.entidades.Paciente;
import es.uvigo.esei.dagss.dominio.entidades.Receta;
import es.uvigo.esei.dagss.dominio.entidades.TipoUsuario;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author ribadas
 */
@Named(value = "pacienteControlador")
@SessionScoped
public class PacienteControlador implements Serializable {

    private Paciente pacienteActual;
    private String dni;
    private String numeroTarjetaSanitaria;
    private String numeroSeguridadSocial;
    private String password;
    
    private List<Cita> listadoCitas;
    private List<Receta> listadoRecetas;
    private List<Date> listadoHoras;
    private Cita nuevaCita;

    @Inject
    private AutenticacionControlador autenticacionControlador;

    @Inject
    private PacienteDAO pacienteDAO;
    
    @EJB
    private CitaDAO citaDAO;
    
    @EJB
    private RecetaDAO recetaDAO;

    /**
     * Creates a new instance of AdministradorControlador
     */
    public PacienteControlador() {
    }

    public Cita getNuevaCita() {
        return nuevaCita;
    }

    public void setNuevaCita(Cita nuevaCita) {
        this.nuevaCita = nuevaCita;
    }
    
    public List<Cita> getListadoCitas() {
        return listadoCitas;
    }

    public void setListadoCitas(List<Cita> listadoCitas) {
        this.listadoCitas = listadoCitas;
    }

    public List<Receta> getListadoRecetas() {
        return listadoRecetas;
    }

    public void setListadoRecetas(List<Receta> listadoRecetas) {
        this.listadoRecetas = listadoRecetas;
    }

    public List<Date> getListadoHoras() {
        return listadoHoras;
    }

    public void setListadoHoras(List<Date> listadoHoras) {
        this.listadoHoras = listadoHoras;
    }
    
    @PostConstruct
    public void inicializar() {
    }

    public Paciente getPacienteActual() {
        return pacienteActual;
    }

    public void setPacienteActual(Paciente pacienteActual) {
        this.pacienteActual = pacienteActual;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumeroTarjetaSanitaria() {
        return numeroTarjetaSanitaria;
    }

    public void setNumeroTarjetaSanitaria(String numeroTarjetaSanitaria) {
        this.numeroTarjetaSanitaria = numeroTarjetaSanitaria;
    }

    public String getNumeroSeguridadSocial() {
        return numeroSeguridadSocial;
    }

    public void setNumeroSeguridadSocial(String numeroSeguridadSocial) {
        this.numeroSeguridadSocial = numeroSeguridadSocial;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private boolean parametrosAccesoInvalidos() {
        return (((dni == null) && (numeroSeguridadSocial == null) && (numeroTarjetaSanitaria == null))
                || (password == null));
    }

    private Paciente recuperarDatosPaciente() {
        Paciente paciente = null;
        if (dni != null) {
            paciente = pacienteDAO.buscarPorDNI(dni);
        }
        if ((paciente == null) && (numeroSeguridadSocial != null)) {
            paciente = pacienteDAO.buscarPorNumeroSeguridadSocial(numeroSeguridadSocial);
        }
        if ((paciente == null) && (numeroTarjetaSanitaria != null)) {
            paciente = pacienteDAO.buscarPorTarjetaSanitaria(numeroTarjetaSanitaria);
        }
        return paciente;
    }

    public String doLogin() {
        String destino = null;
        if (parametrosAccesoInvalidos()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No se ha indicado suficientes datos de autenticación", ""));
        } else {
            Paciente paciente = recuperarDatosPaciente();
            if (paciente == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningún paciente con los datos indicados", ""));
            } else {
                if (autenticacionControlador.autenticarUsuario(
                        paciente.getId(),
                        password,
                        TipoUsuario.PACIENTE.getEtiqueta().toLowerCase())) {

                    pacienteActual = paciente;

// TODO:  revisar acceso sin password en primer uso
//                    if (paciente.getPassword().equals("")) {
//                        destino = "privado/cambiarPassword";
//                    } else {
                        destino = "privado/index";
//                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }
            }
        }
        return destino;
    }
    
    //devuelve las lista de citas actuales del paciente logueado (error si el paciente no esta logueado)
    public String getCitasPaciente(){
        if(this.pacienteActual!=null){
            List<Cita> citas=this.citaDAO.buscarPorNSS(this.pacienteActual.getNumeroSeguridadSocial());
            for(Cita cita:citas) if(cita.getFecha().getTime()<new Date().getTime()) citas.remove(cita);
            this.listadoCitas=citas;
            return "lista_citas";
        }
        else return "error";
    }
    
    //anular una cita por realizar
    public void anularCita(Cita cita){
        if(this.autenticacionControlador.isUsuarioAutenticado() && cita.getPaciente().getId().equals(this.pacienteActual.getId())){
            if(cita.getEstado().equals(EstadoCita.PLANIFICADA)) cita.setEstado(EstadoCita.ANULADA);
            this.citaDAO.actualizar(cita);
        }
    }
    
    //devuelve las lista de recetas actuales del paciente logueado (error si el paciente no esta logueado o no tiene contraseña)
    public String getRecetasPaciente(){
        if(this.pacienteActual!=null && this.getPassword()!=null){
            List<Receta> recetas=this.recetaDAO.buscarPorNumTarjeta(this.pacienteActual.getNumeroTarjetaSanitaria());
            for(Receta receta:recetas) if(!receta.enFecha() || !receta.getEstado().equals(EstadoReceta.GENERADA)) recetas.remove(receta);
            this.listadoRecetas=recetas;
            return "lista_recetas";
        }
        else return "error";
    }
    
    //modifica los datos de la farmacia logeada
    public void cambiarDatosPacienteActual(String pass,String nombre,String apellidos,Direccion dir,String telefono,String email){
        if(this.pacienteActual!=null && this.getPassword()!=null){
            if(pass!=null){
                this.setPassword(pass);
                this.pacienteActual.setPassword(pass);
            }
            if(nombre!=null) this.pacienteActual.setNombre(nombre);
            if(apellidos!=null) this.pacienteActual.setApellidos(apellidos);
            if(dir!=null) this.pacienteActual.setDireccion(dir);
            if(telefono!=null) this.pacienteActual.setTelefono(telefono);
            if(email!=null) this.pacienteActual.setEmail(email);
            this.pacienteDAO.actualizar(this.pacienteActual);
        }
    }
    
    //para una fecha dada muestra los huecos existentes para una cita
    public String mostrarHorasCita(Date fecha){
        List<Date> toret=new ArrayList<>();
        List<Cita> citas=this.citaDAO.buscarPorFecha(fecha.toString());
        for(int hour=0;hour<=23;hour++){
            for(int min=0;min<=45;min+=15){
                boolean libre=true;
                for(Cita cita:citas) if(cita.getHora().getHours()==hour && cita.getHora().getMinutes()==min) libre=false;
                if(libre){
                    fecha.setHours(hour);
                    fecha.setMinutes(min);
                    toret.add(fecha);
                }
            }
        }
        this.listadoHoras=toret;
        return "crear_cita";
    }
    
    public void crearCita(){
        this.nuevaCita.setEstado(EstadoCita.PLANIFICADA);
        this.nuevaCita.setMedico(this.pacienteActual.getMedico());
        this.nuevaCita.setPaciente(this.pacienteActual);
        this.citaDAO.crear(this.nuevaCita);
    }
}