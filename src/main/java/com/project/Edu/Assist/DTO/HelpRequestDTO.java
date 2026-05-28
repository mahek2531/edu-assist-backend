package com.project.Edu.Assist.DTO;
public class HelpRequestDTO {

    private String type;
    private String name;
    private String email;
    private String subject;
    private String message;

    public HelpRequestDTO() {
    }

    public HelpRequestDTO(String type, String name, String email, String subject, String message) {
        this.type = type;
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}