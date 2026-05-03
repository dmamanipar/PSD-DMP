package pe.edu.upeu.sysalmacen;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {
    //Pruebas de cambios para probar pepilene segunda prueba
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SysAlmacenApplication.class);
	}

}
