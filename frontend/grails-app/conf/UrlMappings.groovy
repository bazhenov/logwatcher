class UrlMappings {

	static mappings = {
		"/remove-entry/$checksum"(controller: 'index', action: "removeEntry")
		"/$date?"(controller: 'index', action: "index")
		"500"(view: '/error')
	}
}
