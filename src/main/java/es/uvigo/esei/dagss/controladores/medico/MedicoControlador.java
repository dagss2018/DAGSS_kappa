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
import es.uvigo.esei.dagss.dominio.entidades.EstadoCita;
import es.uvigo.esei.dagss.dominio.entidades.Medicamento;
import es.uvigo.esei.dagss.dominio.entidades.Medico;
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
                    medicoActual = medico;
                    destino = "privado/index";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }
            }
        }
        return destino;
    }

    //Acciones
    public String doShowCita() {
        return "detallesCita";
    }
    
    //devuelve la lista de citas del dia que tiene el medico registrado (null si no hay medico registrado)
    public List<Cita> getCitasDia(){
        if(this.medicoActual!=null){
            Date date=new Date();
            List<Cita> toret=this.citaDAO.buscarPorDniMedico(this.medicoActual.getDni());
            for(Cita cita:toret) if(cita.getFecha().getYear()!=date.getYear() || cita.getFecha().getMonth()!=date.getMonth() || cita.getFecha().getDate()!=date.getDate()) toret.remove(cita);
            return toret;
        }
        else return null;
    }
    
    //devuelve si la cita esta en estado 'planificada' y cambia este a 'completada'
    public boolean atenderCita(String idCita){
        if(this.autenticacionControlador.isUsuarioAutenticado()){
            Cita cita=this.citaDAO.buscarPorId(idCita);
            if(cita.getEstado().equals(EstadoCita.PLANIFICADA)) cita.setEstado(EstadoCita.COMPLETADA);
            return cita.getEstado().equals(EstadoCita.PLANIFICADA);
        }
        else return false;
    }
    
    //devuelve las prescripciones activas de un paciente
    public List<Prescripcion> getPrescripcionesPaciente(String idPaciente){
        Date date=new Date();
        List<Prescripcion> toret=this.prescripcionDAO.buscarPorIdPaciente(idPaciente);
        for(Prescripcion p:toret) if(p.getFechaFin().getTime()>=date.getTime()) toret.remove(p);
        return toret;
    }
    
    //devuelve el resultado de la busqueda de un medicamento (form)
    public List<Medicamento> getMedicamentos(String name,String prinActivo,String fabricante){
        if(name.isEmpty() && prinActivo.isEmpty() && fabricante.isEmpty()) return this.medicamentoDAO.buscarTodos(); //si el form esta vacio devuelve todos los medicamentos
        else return this.medicamentoDAO.busquedaForm(name, prinActivo, fabricante);
    }
    
    public void crearPrescripcion(String dosis,Date fFin,Date fInicio,String indicaciones,String idMedicamento,String idMedico,String idPaciente){
        this.prescripcionDAO.crear(new Prescripcion(this.pacienteDAO.buscarPorId(idPaciente),this.medicamentoDAO.buscarPorId(idMedicamento),this.medicoActual,Integer.parseInt(dosis),indicaciones,fInicio,fFin));
    }
    
    //elimina una prescripcion dada (y todas sus recetas asignadas)
    public void borrarPrescripcion(String idPrescripcion){
        Prescripcion p=this.prescripcionDAO.buscarPorId(idPrescripcion);
        List<Receta> recetas=p.getRecetas();
        this.prescripcionDAO.eliminar(p);
        for(Receta receta:recetas) this.recetaDAO.eliminar(receta);
    }
}