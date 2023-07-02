package example.avaje.typeuse;

import java.util.List;
import java.util.Map;

import io.avaje.validation.ValidPojo;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.NotNull;

@ValidPojo
public record Ship(
    Map<
            @NotNull(message = "Names cannot be null") @NotBlank(message = "Names cannot be blank")
            String,
            @NotNull(message = "Values cannot be null") @ValidPojo CrewMate>
        crew,
    List<
            @NotNull(message = "Tasks cannot be null") @NotBlank(message = "Tasks cannot be blank")
            String>
        tasks) {}
