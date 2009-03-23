class UrlMappings {

	static mappings = {
		"/$date?"(controller: 'index', action: "index")
		"500"(view: '/error')
	}
}
