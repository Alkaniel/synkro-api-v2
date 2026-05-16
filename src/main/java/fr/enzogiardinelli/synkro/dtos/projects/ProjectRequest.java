package fr.enzogiardinelli.synkro.dtos.projects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProjectRequest {

    @NotNull
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;
    private String description;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
