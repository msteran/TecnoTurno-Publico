package co.edu.uts.tecnoturno.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalErrors {
  @ExceptionHandler({IllegalArgumentException.class, java.util.NoSuchElementException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String missing(Exception ex, Model m) {
    m.addAttribute("mensaje", "El registro solicitado no existe o los datos no son válidos.");
    return "error/general";
  }

  @ExceptionHandler({
    org.springframework.dao.DataIntegrityViolationException.class,
    org.springframework.orm.ObjectOptimisticLockingFailureException.class
  })
  @ResponseStatus(HttpStatus.CONFLICT)
  String conflict(Exception ex, Model m) {
    m.addAttribute(
        "mensaje",
        "No se pudo guardar: el registro está relacionado, duplicado o fue modificado por otra"
            + " persona. Recargue y revise los datos.");
    return "error/general";
  }
}
