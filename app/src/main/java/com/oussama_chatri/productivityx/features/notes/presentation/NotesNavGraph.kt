package com.oussama_chatri.productivityx.features.notes.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.oussama_chatri.productivityx.features.notes.presentation.editor.NoteEditorScreen
import com.oussama_chatri.productivityx.features.notes.presentation.list.NotesScreen
import com.oussama_chatri.productivityx.features.notes.presentation.organization.FolderManagementScreen
import com.oussama_chatri.productivityx.features.notes.presentation.organization.TagManagementScreen
import com.oussama_chatri.productivityx.features.notes.presentation.organization.TemplateGalleryScreen
import com.oussama_chatri.productivityx.features.notes.presentation.trash.TrashScreen

fun NavGraphBuilder.notesGraph(navController: NavController) {

    composable<NotesRoute.NotesList> {
        NotesScreen(
            onNavigateToEditor = { noteId ->
                navController.navigate(NotesRoute.NoteEditor(noteId))
            },
            onNavigateToSearch = {
                // navController.navigate(AppRoute.Search)
            },
            onNavigateToTrash = {
                navController.navigate(NotesRoute.Trash)
            }
        )
    }

    composable<NotesRoute.NoteEditor> { backstackEntry ->
        val route: NotesRoute.NoteEditor = backstackEntry.toRoute()
        NoteEditorScreen(
            noteId = route.noteId,
            onNavigateBack = { navController.navigateUp() }
        )
    }

    composable<NotesRoute.Trash> {
        TrashScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }

    composable<NotesRoute.TagManagement> {
        TagManagementScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }

    composable<NotesRoute.FolderManagement> {
        FolderManagementScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }

    composable<NotesRoute.TemplateGallery> {
        TemplateGalleryScreen(
            onNavigateBack = { navController.navigateUp() },
            onApplyTemplate = { templateId ->
                navController.navigate(NotesRoute.NoteEditor(null))
            }
        )
    }
}
