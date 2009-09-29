class UrlMappings {

	static mappings = {
		"/remove-entry/$checksum"(controller: 'index', action: "removeEntry")
		"/$date?"(controller: 'index', action: "index")

		"/install/h2"(controller: 'install', action: 'h2')
		"500"(view: '/error')
	}
}
