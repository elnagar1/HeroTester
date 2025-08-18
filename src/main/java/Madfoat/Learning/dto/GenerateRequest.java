package Madfoat.Learning.dto;

import java.util.List;

public class GenerateRequest {
    private String input;
    private String inputType;
    private String generationType;
    private boolean includeAcceptance;
    private List<String> selectedTypes;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getGenerationType() {
        return generationType;
    }

    public void setGenerationType(String generationType) {
        this.generationType = generationType;
    }

    public boolean isIncludeAcceptance() {
        return includeAcceptance;
    }

    public void setIncludeAcceptance(boolean includeAcceptance) {
        this.includeAcceptance = includeAcceptance;
    }

    public List<String> getSelectedTypes() {
        return selectedTypes;
    }

    public void setSelectedTypes(List<String> selectedTypes) {
        this.selectedTypes = selectedTypes;
    }
}
