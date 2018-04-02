package com.ibm.streams.tutorial.exercise2;


public final class RegisteredVoter {

    private final String electorId;

    public RegisteredVoter(String electorId) {
        this.electorId = electorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisteredVoter that = (RegisteredVoter) o;

        if (!electorId.equals(that.getElectorId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return electorId.hashCode();
    }

    @Override
    public String toString() {
        return "RegisteredVoter{" +
                "electorId='" + electorId + '\'' +
                '}';
    }

    public String getElectorId() {
        return electorId;
    }
}
