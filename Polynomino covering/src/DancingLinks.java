import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

class dataObj {
	public dataObj U, D, R, L;
	public int set;
	public colObj C;

	public dataObj() {
		U = null;
		D = null;
		R = null;
		L = null;
	}

	public boolean isNull() {
		return U == null & D == null & L == null & R == null;
	}
}

class colObj<E> extends dataObj {
	public int S;
	public E N;

	public colObj() {
		super();
		S = 0;
	}

	public colObj(E N) {
		this();
		this.N = N;
	}
}

public class DancingLinks<E> extends ExactCover<E> {

	public static colObj h;

	public static LinkedList reference;

	public DancingLinks() {
	}

	public DancingLinks(LinkedList<E> groundSet, LinkedList<Set<E>> collection) {
		super(groundSet, collection);
		reference = (LinkedList) collection;
		h = new colObj();
		h.L = h;
		h.R = h;
		for (E element : groundSet) {
			colObj<E> cur = new colObj<E>(element);
			cur.L = h.L;
			cur.R = h;
			cur.C = cur;
			cur.U = cur;
			cur.D = cur;
			h.L.R = cur;
			h.L = cur;
		}
		for (Set<E> set : collection) {
			dataObj first = new dataObj();
			for (colObj<E> col = (colObj<E>) h.R; !col.equals(h); col = (colObj<E>) col.R) {
				if (set.contains(col.N)) {
					dataObj cur = new dataObj();
					if (first.isNull()) {
						first.L = first;
						first.R = first;
						cur = first;
					} else {
						cur.L = first.L;
						cur.R = first;
						first.L.R = cur;
						first.L = cur;
					}
					cur.U = col.U;
					cur.D = col;
					cur.C = col;
					col.U.D = cur;
					col.U = cur;
					col.S++;
				}
			}
		}
	}

	public void coverColumn(dataObj x) {
		x.R.L = x.L;
		x.L.R = x.R;
		for (dataObj t = x.D; !t.equals(x); t = t.D) {
			for (dataObj y = t.R; !y.equals(t); y = y.R) {
				y.D.U = y.U;
				y.U.D = y.D;
				y.C.S--;
			}
		}
	}

	public void uncoverColumn(dataObj x) {
		x.R.L = x;
		x.L.R = x;
		for (dataObj t = x.U; !t.equals(x); t = t.U)
			for (dataObj y = t.L; !y.equals(t); y = y.L) {
				y.D.U = y;
				y.U.D = y;
				y.C.S++;
			}
	}

	public Set<Set<dataObj>> solution = new HashSet<Set<dataObj>>();
	static Random r = new Random();

	public void exactCover() {
		if (h.R.equals(h)) {
			solution.add(new HashSet<dataObj>());
			return;
		}
		colObj<E> x = (colObj<E>) h.R;
		for (colObj<E> cur = (colObj<E>) x.R; !cur.equals(h); cur = (colObj<E>) cur.R)
			if (cur.S < x.S) {
				x = cur;
			}
		coverColumn(x);
		for (dataObj t = x.U; !t.equals(x); t = t.U) {
////	Add this to code to limit the cases for big tests 
//			if (solution.size() >= 500) {
//				break;
//				}
////	or this for more randomness in choosing a tile to cover x
			if (r.nextFloat() >= 0.7)
				continue;
			for (dataObj y = t.L; !y.equals(t); y = y.L) {
				coverColumn(y.C);
			}
			DancingLinks<E> dls = new DancingLinks<E>();
			dls.exactCover();
			for (Set<dataObj> P : dls.solution) {
				P.add(t);
				solution.add(P);
			}

			for (dataObj y = t.R; !y.equals(t); y = y.R)
				uncoverColumn(y.C);
		}
		uncoverColumn(x);
		return;
	}

	public void printSol() {
		System.out.println("Solution: ");
		for (Set<dataObj> P : solution)
			System.out.println(P);
	}

	public static void Test1() {
		ExactCover<Integer> ECP = new ExactCover<Integer>();
		for (int i = 1; i <= 7; i++) {
			ECP.groundSet.add(i);
		}
		ECP.collection = new LinkedList<Set<Integer>>();
		ECP.collection.add(new HashSet<Integer>(Arrays.asList(new Integer[] { 3, 5, 6 })));
		ECP.collection.add(new HashSet<Integer>(Arrays.asList(new Integer[] { 1, 4, 7 })));
		ECP.collection.add(new HashSet<Integer>(Arrays.asList(new Integer[] { 2, 3, 6 })));
		ECP.collection.add(new HashSet<Integer>(Arrays.asList(new Integer[] { 1, 4 })));
		ECP.collection.add(new HashSet<Integer>(Arrays.asList(new Integer[] { 2, 7 })));
		ECP.collection.add(new HashSet<Integer>(Arrays.asList(new Integer[] { 4, 5, 7 })));
		ECP.collection.add(new HashSet<Integer>(Arrays.asList(new Integer[] { 1 }))); // additional

		long startTime = System.currentTimeMillis();
//		---------------------------------------------------------------------------------------
		DancingLinks<Integer> DLS = new DancingLinks<Integer>(ECP.groundSet, ECP.collection);
		DLS.exactCover();
		DLS.printSol();
//		---------------------------------------------------------------------------------------
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;

		System.out.println("Execution time = " + executionTime + " ms");
	}

	public static void Test2() {
		ExactCover ECP = new ExactCover();
		for (int i = 1; i <= 10; i++) {
			ECP.groundSet.add(i);
		}

////	Choose one of below: all subsets of X or all combination k of X
		ECP.collection.addAll(ExactCover.subsets(new HashSet<Integer>(ECP.groundSet)));
//		ECP.collection.addAll(ExactCover.subsetsK(new HashSet<Integer>(ECP.groundSet), 2));

////	Randomly removing half of the subsets;
//		Random r = new Random();
//		int number = ECP.collection.size();
//		for (int i = 0; i < number/2; i++)
//			ECP.collection.remove(r.nextInt((number - i)));

		ECP.printX();
//		ECP.printC();

		DancingLinks<Integer> DLS = new DancingLinks<Integer>(ECP.groundSet, ECP.collection);
		long startTime = System.currentTimeMillis();
//		-------------------------------------------------------------------------------------

		DLS.exactCover();
		System.out.println("Solution = " + DLS.solution.size());
//		DLS.printSol();
//		-------------------------------------------------------------------------------------

		long endTime = System.currentTimeMillis();

		long executionTime = endTime - startTime;

		System.out.println("Execution time = " + executionTime + " ms");
	}

	public static void main(String[] args) {
//		Test1();
//		Test2();
	}
}
