package io.left.ripple;

public enum Colour {
    RED(R.color.red),
    GREEN(R.color.green),
    BLUE(R.color.blue);

    private int colourId;

    // getter method
    public int getColourId()
    {
        return this.colourId;
    }

    Colour(int colourId)
    {
        this.colourId = colourId;
    }
}
