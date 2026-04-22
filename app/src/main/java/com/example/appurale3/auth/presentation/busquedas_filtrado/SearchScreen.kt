package com.example.appurale3.auth.presentation.busquedas_filtrado
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    vm: SearchViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { vm.onQueryChange(it) },
                        placeholder = { Text("Buscar actividades o rutinas...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.toggleFilterMenu() }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            // 🔽 FILTROS
            if (uiState.showFilters) {
                FilterSection(uiState, vm)
            }

            // 🔎 RESULTADOS
            LazyColumn {
                items(uiState.results) { item ->
                    when (item) {
                        is SearchItem.ActivityItem -> {
                            ActivityResultItem(item.activity)
                        }
                        is SearchItem.RoutineItem -> {
                            RoutineResultItem(item.routine)
                        }
                    }
                }
            }
        }
    }
}
*/