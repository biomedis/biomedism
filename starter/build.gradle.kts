
group = "ru.biomedis"
version = "1.2.0"



repositories{
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation("io.vertx", "vertx-web-client", "3.4.2")

}

application {
	mainClassName = "ru.biomedis.starter.App"
}

	
