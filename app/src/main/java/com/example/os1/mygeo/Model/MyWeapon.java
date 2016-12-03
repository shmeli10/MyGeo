package com.example.os1.mygeo.Model;

/**
 * Created by OS1 on 27.10.2016.
 */
public class MyWeapon {

    private String id;
    private String type;
    private String damage;
    private int quantity;
    private int speedFly;
    private String strength;
    private String attackRange;

    private boolean is_selected;

    ////////////////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getType() {
        return type;
    }

    public void setType(String value) {
        type = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getDamage() {
        return damage;
    }

    public void setDamage(String value) {
        damage = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(String value) {

        int intValue = 0;

        if((value != null) && (!value.equals("")))
            intValue = Integer.parseInt(value);

        quantity = intValue;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getSpeedFly() {
        return speedFly;
    }

    public void setSpeedFly(String value) {

        int intValue = 0;

        if((value != null) && (!value.equals("")))
            intValue = Integer.parseInt(value);

        speedFly = intValue;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getStrength() {
        return strength;
    }

    public void setStrength(String value) {

        int intValue = 0;

        if((value != null) && (!value.equals("")))
            intValue = Integer.parseInt(value);

        strength = "" +intValue;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(String value) {

        int intValue = 0;

        if((value != null) && (!value.equals("")))
            intValue = Integer.parseInt(value);

        attackRange = "" +intValue;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean isSelected() {
        return is_selected;
    }

    // public void setIsSelected(String value) {
    public void setIsSelected(boolean value) {

//        if(value.equals("1"))
//            is_selected = true;
//        else
//            is_selected = false;

        is_selected = value;
    }
}