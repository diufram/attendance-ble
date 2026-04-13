package com.example.attendance.view

import com.example.attendance.model.MateriaModel
import kotlinx.coroutines.flow.StateFlow

interface IMateriaDocenteView {
    val materias: StateFlow<List<MateriaModel>>
    val siglaFormulario: StateFlow<String>
    val nombreFormulario: StateFlow<String>
    val grupoFormulario: StateFlow<String>
    val periodoFormulario: StateFlow<String>
    val mostrarModalCrearMateria: StateFlow<Boolean>
    val mostrarModalEditarMateria: StateFlow<Boolean>
    val mostrarModalEliminarMateria: StateFlow<Boolean>
    val materiaSeleccionadaAccion: StateFlow<MateriaModel?>
    val errorMensaje: StateFlow<String?>

    fun iniciar()
    fun onCerrarSesion()
    fun onMateriaSeleccionada(materia: MateriaModel)
    fun onSiglaFormularioChange(valor: String)
    fun onNombreFormularioChange(valor: String)
    fun onGrupoFormularioChange(valor: String)
    fun onPeriodoFormularioChange(valor: String)
    fun onAbrirModalCrear()
    fun onCerrarModalCrear()
    fun onCrearMateria()
    fun onAbrirModalEditar(materia: MateriaModel)
    fun onCerrarModalEditar()
    fun onGuardarEdicion()
    fun onAbrirModalEliminar(materia: MateriaModel)
    fun onCerrarModalEliminar()
    fun onConfirmarEliminar()
    fun onCerrarError()
}
