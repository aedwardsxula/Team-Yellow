package com.example.insurance.model;

public final class InsuranceRecord {
    public final int age;
    public final String sex;
    public final double bmi;
    public final int children;
    public final String smoker;
    public final String region;
    public final double charges;

    public InsuranceRecord(int age, String sex, double bmi, int children, String smoker, String region, double charges) {
        this.age = age;
        this.sex = sex == null ? "" : sex;
        this.bmi = bmi;
        this.children = children;
        this.smoker = smoker == null ? "" : smoker;
        this.region = region == null ? "" : region;
        this.charges = charges;
    }

    @Override
    public String toString() {
        return String.format(
            "Age: %d | Sex: %s | BMI: %.2f | Children: %d | Smoker: %s | Region: %s | Charges: %.2f",
            age, sex, bmi, children, smoker, region, charges
        );
    }
}
