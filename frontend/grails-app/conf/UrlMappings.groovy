class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?" {
			constraints {
				// apply constraints here
			}
		}
		"/"(controller: 'index', action: "index")
		"500"(view: '/error')
	}
}
