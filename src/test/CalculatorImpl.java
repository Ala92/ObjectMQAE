package test;

import java.math.BigInteger;
import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class CalculatorImpl extends RemoteObject implements Calculator {
	private int mult = 0;

	public CalculatorImpl() throws Exception {
		super();
	}

	private static final long serialVersionUID = 1L;

        @Override
	public void add(int x, int y) {
            System.out.println(x+"+"+y +"= " + (x+y));
	}

	@Override
	public void mult(int x, int y) {
		mult = x * y;
                System.out.println(x+"*"+y +"= " + (x*y));

	}

	public int getMult() {
		return mult;
	}

	public void setMult(int mult) {
		this.mult = mult;
	}

	@Override
	public int divideByZero() {
		int x = 2 / 0;
		return x;
	}

    @Override
    public void fact(int x)  {
        BigInteger n = new BigInteger(""+x);
    BigInteger result = BigInteger.ONE;

    while (!n.equals(BigInteger.ZERO)) {
        result = result.multiply(n);
        n = n.subtract(BigInteger.ONE);
    }
    System.out.println (result);
    }

}
