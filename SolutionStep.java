
public class SolutionStep {

    private Factory factory;
    private Warehouse warehouse;

    private int scale = 0;

    public Factory getFactory() {
	return factory;
    }

    public void setFactory(Factory factory) {
	this.factory = factory;
    }

    public Warehouse getWarehouse() {
	return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
	this.warehouse = warehouse;
    }

    public int getScale() {
	return scale;
    }

    public void setScale(int scale) {
	this.scale = scale;
    }

}