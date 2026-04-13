package com.example.attendance.controller

import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation
import com.example.attendance.view.IMateriaDocenteView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MateriaDocenteController(
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) : IMateriaDocenteView {

    private val _siglaFormulario = MutableStateFlow("")
    override val siglaFormulario: StateFlow<String> = _siglaFormulario.asStateFlow()

    private val _nombreFormulario = MutableStateFlow("")
    override val nombreFormulario: StateFlow<String> = _nombreFormulario.asStateFlow()

    private val _grupoFormulario = MutableStateFlow("")
    override val grupoFormulario: StateFlow<String> = _grupoFormulario.asStateFlow()

    private val _periodoFormulario = MutableStateFlow("")
    override val periodoFormulario: StateFlow<String> = _periodoFormulario.asStateFlow()

    private val _materias = MutableStateFlow<List<MateriaModel>>(emptyList())
    override val materias: StateFlow<List<MateriaModel>> = _materias.asStateFlow()

    private val _mostrarModalCrearMateria = MutableStateFlow(false)
    override val mostrarModalCrearMateria: StateFlow<Boolean> = _mostrarModalCrearMateria.asStateFlow()

    private val _mostrarModalEditarMateria = MutableStateFlow(false)
    override val mostrarModalEditarMateria: StateFlow<Boolean> = _mostrarModalEditarMateria.asStateFlow()

    private val _mostrarModalEliminarMateria = MutableStateFlow(false)
    override val mostrarModalEliminarMateria: StateFlow<Boolean> = _mostrarModalEliminarMateria.asStateFlow()

    private val _materiaSeleccionadaAccion = MutableStateFlow<MateriaModel?>(null)
    override val materiaSeleccionadaAccion: StateFlow<MateriaModel?> = _materiaSeleccionadaAccion.asStateFlow()

    private val _errorMensaje = MutableStateFlow<String?>(null)
    override val errorMensaje: StateFlow<String?> = _errorMensaje.asStateFlow()

    override fun iniciar() {
        val carnet = materiaModel.usuarioCarnet.value ?: return
        materiaModel.cargarMaterias(carnet, esDocente = true)
        _materias.value = materiaModel.materiasUsuario.value
    }

    override fun onCerrarSesion() {
        materiaModel.limpiarMaterias()
        navigator.irLoginView()
    }

    override fun onMateriaSeleccionada(materia: MateriaModel) {
        navigator.irAsistenciaView(materia.id)
    }

    override fun onSiglaFormularioChange(valor: String) {
        _siglaFormulario.value = valor
    }

    override fun onNombreFormularioChange(valor: String) {
        _nombreFormulario.value = valor
    }

    override fun onGrupoFormularioChange(valor: String) {
        _grupoFormulario.value = valor
    }

    override fun onPeriodoFormularioChange(valor: String) {
        _periodoFormulario.value = valor
    }

    override fun onAbrirModalCrear() {
        _errorMensaje.value = null
        limpiarFormulario()
        _mostrarModalCrearMateria.value = true
    }

    override fun onCerrarModalCrear() {
        _mostrarModalCrearMateria.value = false
        limpiarFormulario()
    }

    override fun onCrearMateria() {
        val carnet = materiaModel.usuarioCarnet.value ?: run {
            _errorMensaje.value = "No hay sesión activa"
            return
        }

        val creada = materiaModel.crear(
            MateriaModel(
                sigla = _siglaFormulario.value.trim(),
                nombre = _nombreFormulario.value.trim(),
                grupo = _grupoFormulario.value.trim(),
                periodo = _periodoFormulario.value.trim(),
                docenteCarnet = carnet,
            )
        )

        if (creada == null) {
            _errorMensaje.value = "No se pudo crear la materia"
            return
        }

        materiaModel.cargarMaterias(carnet, esDocente = true)
        _materias.value = materiaModel.materiasUsuario.value
        _mostrarModalCrearMateria.value = false
        limpiarFormulario()
    }

    override fun onAbrirModalEditar(materia: MateriaModel) {
        _errorMensaje.value = null
        _materiaSeleccionadaAccion.value = materia
        _siglaFormulario.value = materia.sigla
        _nombreFormulario.value = materia.nombre
        _grupoFormulario.value = materia.grupo
        _periodoFormulario.value = materia.periodo
        _mostrarModalEditarMateria.value = true
    }

    override fun onCerrarModalEditar() {
        _mostrarModalEditarMateria.value = false
        _materiaSeleccionadaAccion.value = null
        limpiarFormulario()
    }

    override fun onGuardarEdicion() {
        val carnet = materiaModel.usuarioCarnet.value ?: run {
            _errorMensaje.value = "No hay sesión activa"
            return
        }

        val base = _materiaSeleccionadaAccion.value ?: return

        val materia = MateriaModel(
            id = base.id,
            sigla = _siglaFormulario.value.trim(),
            nombre = _nombreFormulario.value.trim(),
            grupo = _grupoFormulario.value.trim(),
            periodo = _periodoFormulario.value.trim(),
            docenteCarnet = base.docenteCarnet,
            bitmapIndexEstudiante = base.bitmapIndexEstudiante,
        )

        val actualizado = materiaModel.editar(materia)
        if (!actualizado) {
            _errorMensaje.value = "No se pudo editar la materia"
            return
        }

        materiaModel.cargarMaterias(carnet, esDocente = true)
        _materias.value = materiaModel.materiasUsuario.value
        _mostrarModalEditarMateria.value = false
        _materiaSeleccionadaAccion.value = null
        limpiarFormulario()
    }

    override fun onAbrirModalEliminar(materia: MateriaModel) {
        _errorMensaje.value = null
        _materiaSeleccionadaAccion.value = materia
        _mostrarModalEliminarMateria.value = true
    }

    override fun onCerrarModalEliminar() {
        _mostrarModalEliminarMateria.value = false
        _materiaSeleccionadaAccion.value = null
    }

    override fun onConfirmarEliminar() {
        val carnet = materiaModel.usuarioCarnet.value ?: run {
            _errorMensaje.value = "No hay sesión activa"
            return
        }

        val materia = _materiaSeleccionadaAccion.value ?: return
        val eliminado = materiaModel.eliminar(materia)
        if (!eliminado) {
            _errorMensaje.value = "No se pudo eliminar la materia"
            return
        }

        materiaModel.cargarMaterias(carnet, esDocente = true)
        _materias.value = materiaModel.materiasUsuario.value
        _mostrarModalEliminarMateria.value = false
        _materiaSeleccionadaAccion.value = null
    }

    override fun onCerrarError() {
        _errorMensaje.value = null
    }

    private fun limpiarFormulario() {
        _siglaFormulario.value = ""
        _nombreFormulario.value = ""
        _grupoFormulario.value = ""
        _periodoFormulario.value = ""
    }
}
