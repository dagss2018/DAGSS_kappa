/*
 Proyecto Java EE, DAGSS-2013
 */
package es.uvigo.esei.dagss.controladores.medico;

import es.uvigo.esei.dagss.controladores.autenticacion.AutenticacionControlador;
import es.uvigo.esei.dagss.dominio.daos.CitaDAO;
import es.uvigo.esei.dagss.dominio.daos.MedicamentoDAO;
import es.uvigo.esei.dagss.dominio.daos.MedicoDAO;
import es.uvigo.esei.dagss.dominio.daos.PacienteDAO;
import es.uvigo.esei.dagss.dominio.daos.PrescripcionDAO;
import es.uvigo.esei.dagss.dominio.daos.RecetaDAO;
import es.uvigo.esei.dagss.dominio.entidades.Cita;
import es.uvigo.esei.dagss.dominio.entidades.Direccion;
import es.uvigo.esei.dagss.dominio.entidades.EstadoCita;
import es.uvigo.esei.dagss.dominio.entidades.Medicamento;
import es.uvigo.esei.dagss.dominio.entidades.Medico;
import es.uvigo.esei.dagss.dominio.entidades.Paciente;
import es.uvigo.esei.dagss.dominio.entidades.Prescripcion;
import es.uvigo.esei.dagss.dominio.entidades.Receta;
import es.uvigo.esei.dagss.dominio.entidades.TipoUsuario;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author ribadas
 */

@Named(value = "medicoControlador")
@SessionScoped
public class MedicoControlador implements Serializable {

    private Medico medicoActual;
    private String dni;
    private String numeroColegiado;
    private String password;
    
    private List<Cita> listadoCitas;
    private List<Prescripcion> listadoPrescripciones;
    private List<Medicamento> listadoMedicamentos;
    private Prescripcion nuevaPrescripcion;
    private String textoBusqueda;
    private Cita citaActual;

    @Inject
    private AutenticacionControlador autenticacionControlador;
    

    @EJB
    private MedicoDAO medicoDAO;
    
    @EJB
    private CitaDAO citaDAO;
    
    @EJB
    private PrescripcionDAO prescripcionDAO;
    
    @EJB
    private MedicamentoDAO medicamentoDAO;
    
    @EJB
    private PacienteDAO pacienteDAO;
    
    @EJB
    private RecetaDAO recetaDAO;

    /**
     * Creates a new instance of AdministradorControlador
     */
    public MedicoControlador() {
    }

    public Cita getCitaActual(){
        return citaActual; 
    }
    
    public void setCitaActual(Cita cita){
        this.citaActual=cita;
    }
    
    public List<Cita> getListadoCitas() {
        return listadoCitas;
    }

    public void setListadoCitas(List<Cita> listadoCitas) {
        this.listadoCitas = listadoCitas;
    }

    public List<Prescripcion> getListadoPrescripciones() {
        return listadoPrescripciones;
    }

    public void setListadoPrescripciones(List<Prescripcion> listadoPrescripciones) {
        this.listadoPrescripciones = listadoPrescripciones;
    }

    public List<Medicamento> getListadoMedicamentos() {
        return listadoMedicamentos;
    }

    public void setListadoMedicamentos(List<Medicamento> listadoMedicamentos) {
        this.listadoMedicamentos = listadoMedicamentos;
    }

    public Prescripcion getNuevaPrescripcion() {
        return nuevaPrescripcion;
    }

    public void setNuevaPrescripcion(Prescripcion nuevaPrescripcion) {
        this.nuevaPrescripcion = nuevaPrescripcion;
    }

    public String getTextoBusqueda() {
        return textoBusqueda;
    }

    public void setTextoBusqueda(String textoBusqueda) {
        this.textoBusqueda = textoBusqueda;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumeroColegiado() {
        return numeroColegiado;
    }

    public void setNumeroColegiado(String numeroColegiado) {
        this.numeroColegiado = numeroColegiado;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Medico getMedicoActual() {
        return medicoActual;
    }

    public void setMedicoActual(Medico medicoActual) {
        this.medicoActual = medicoActual;
    }

    private boolean parametrosAccesoInvalidos() {
        return (((dni == null) && (numeroColegiado == null)) || (password == null));
    }

    private Medico recuperarDatosMedico() {
        Medico medico = null;
        if (dni != null) {
            medico = medicoDAO.buscarPorDNI(dni);
        }
        if ((medico == null) && (numeroColegiado != null)) {
            medico = medicoDAO.buscarPorNumeroColegiado(numeroColegiado);
        }
        return medico;
    }

    public String doLogin() {
        String destino = null;
        if (parametrosAccesoInvalidos()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No se ha indicado un DNI ó un número de colegiado o una contraseña", ""));
        } else {
            Medico medico = recuperarDatosMedico();
            if (medico == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningún médico con los datos indicados", ""));
            } else {
                if (autenticacionControlador.autenticarUsuario(medico.getId(), password,
                        TipoUsuario.MEDICO.getEtiqueta().toLowerCase())) {
                    this.medicoActual = medico;
                    destino = "privado/index";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }
            }
        }
        return destino;
    }

    //Acciones
    public String doShowCita(Cita cita) {
        setCitaActual(cita);
        return "detallesCita";
    }
    
    //devuelve la lista de citas del dia que tiene el medico registrado (error si no hay medico registrado)
    public void getCitasDia(){
        if(this.autenticacionControlador.isUsuarioAutenticado()){
            Date date=new Date();
            List<Cita> toret=this.citaDAO.buscarPorDniMedico(this.medicoActual.getDni());
            for(Cita cita:toret) if(!(date.getTime()>=cita.getFecha().getTime())||(date.getTime()<(cita.getFecha().getTime() + 86400000))) toret.remove(cita);
            setListadoCitas(toret);
        }
        
    }
    
    //devuelve si la cita esta en estado 'planificada'
    public boolean atenderCita(Cita cita){
        if(this.medicoActual==null) return false;
        else return cita.getEstado().equals(EstadoCita.PLANIFICADA) && cita.getMedico().getId().equals(this.medicoActual.getId());
    }
    
    //devuelve las prescripciones activas de un paciente
    public void getPrescripcionesPaciente(Paciente paciente){
        if(this.autenticacionControlador.isUsuarioAutenticado()){
            Date date=new Date();
            List<Prescripcion> toret=this.prescripcionDAO.buscarPorDniPaciente(paciente.getDni());
            for(Prescripcion prescripcion:toret) if((date.getTime()>=prescripcion.getFechaFin().getTime())) toret.remove(prescripcion);
            setListadoPrescripciones(toret);
        }
    }
    
    //devuelve el resultado de la busqueda de un medicamento (form)
    public String getMedicamentosPorNombre(){
        if(this.textoBusqueda.isEmpty()){
            this.listadoMedicamentos=this.medicamentoDAO.buscarTodos();
            return "crear_prescripcion";
        } //si el form esta vacio devuelve todos los medicamentos
        else{
            this.listadoMedicamentos=this.medicamentoDAO.busquedaPorNombre(this.textoBusqueda);
            return "crear_prescripcion";
        }
    }
    
    //devuelve el resultado de la busqueda de un medicamento (form)
    public String getMedicamentosPorPrinActivo(){
        if(this.textoBusqueda.isEmpty()){
            this.listadoMedicamentos=this.medicamentoDAO.buscarTodos();
            return "crear_prescripcion";
        } //si el form esta vacio devuelve todos los medicamentos
        else{
            this.listadoMedicamentos=this.medicamentoDAO.busquedaPorPrinActivo(this.textoBusqueda);
            return "crear_prescripcion";
        }
    }
    
    //devuelve el resultado de la busqueda de un medicamento (form)
    public String getMedicamentosPorFabricante(){
        if(this.textoBusqueda.isEmpty()){
            this.listadoMedicamentos=this.medicamentoDAO.buscarTodos();
            return "crear_prescripcion";
        } //si el form esta vacio devuelve todos los medicamentos
        else{
            this.listadoMedicamentos=this.medicamentoDAO.busquedaPorFabricante(this.textoBusqueda);
            return "crear_prescripcion";
        }
    }
    
    //crea la prescripcion para un paciente dado (pasar recetas en null y prescripcionDAO se encarga de crearlas)
    public void crearPrescripcion(){
        this.nuevaPrescripcion.setMedico(this.medicoActual);
        this.prescripcionDAO.crear(this.nuevaPrescripcion);
    }
    
    //elimina una prescripcion dada (y todas sus recetas asignadas)
    public void borrarPrescripcion(Prescripcion p){
        this.listadoPrescripciones.remove(p);
        this.prescripcionDAO.eliminar(p);
    }
    
    public String finalizarCita(Cita cita){
        if(this.autenticacionControlador.isUsuarioAutenticado()){
            if(cita.getEstado().equals(EstadoCita.PLANIFICADA)) cita.setEstado(EstadoCita.COMPLETADA);
            this.citaDAO.actualizar(cita);
        }
        return "index";
    }
    
    public void notificarAusente(Cita cita){
        if(this.autenticacionControlador.isUsuarioAutenticado()){
            if(cita.getEstado().equals(EstadoCita.PLANIFICADA)) cita.setEstado(EstadoCita.AUSENTE);
            this.citaDAO.actualizar(cita);
        }
    }
    
    public void notificarPendiente(Cita cita){
        if(this.autenticacionControlador.isUsuarioAutenticado()){
            if(cita.getEstado().equals(EstadoCita.AUSENTE)) cita.setEstado(EstadoCita.PLANIFICADA);
            this.citaDAO.actualizar(cita);
        }
    }
    
    
    public void cambiarDatosMedicoActual(String pass,String nombre,String apellidos,String telefono,String email){
        if(this.medicoActual!=null && this.getPassword()!=null){
            if(pass!=null){
                this.setPassword(pass);
                this.medicoActual.setPassword(pass);
            }
            if(nombre!=null) this.medicoActual.setNombre(nombre);
            if(apellidos!=null) this.medicoActual.setApellidos(apellidos);
            if(telefono!=null) this.medicoActual.setTelefono(telefono);
            if(email!=null) this.medicoActual.setEmail(email);
            this.medicoDAO.actualizar(this.medicoActual);
        }
    }
}