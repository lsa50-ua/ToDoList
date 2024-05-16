package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.LOCKED, reason="Usuario bloqueado por el administrador.")
public class UsuarioBloqueadoException extends RuntimeException {
}