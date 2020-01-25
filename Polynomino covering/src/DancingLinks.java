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

	public String toString() {
		String s = "";
		if (C.equals(this))
			return "Element " + C.N.toString() + " contained in " + C.S;
//		s += "Element " + C.N.toString() + " in [";
		s += "[";
		dataObj cur = this;
		while (true) {
			s += cur.C.N.toString();
			cur = cur.R;
			if (cur.equals(this))
				break;
			else
				s += ", ";
		}
		s += "]";
//		s += " contained by " + C.S;
		return s;
	}
}

class colObj<E> extends dataObj {
	int S;
	public E N;
	dataObj last;

	public colObj() {
		super();
		last = this;
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

//	public Set<E> getSet(dataObj data) {
//		dataObj cur = data;
//		Set<E> set = new HashSet<E>();
//		while (true) {
//			set.add((E) cur.C.N);
//			cur = cur.R;
//			if (cur.equals(data))
//				break;
//		}
//		return set;
//	}
//
//	public Set<Set<E>> getSol(Set<dataObj> sol) {
//		Set<Set<E>> set = new HashSet<Set<E>>();
//		for (dataObj s : sol) {
//			set.add(getSet(s));
//		}
//		return set;
//	}
//	
//	public Set<Set<Set<E>>> getSolution() {
//		Set<Set<Set<E>>> list = new HashSet<Set<Set<E>>>();
//		for (Set<dataObj> sol : solution) {
//			list.add(getSol(sol));
//		}
//		return list;
//	}

	public DancingLinks() {
//		this.h = h;
//		this.getSet = getSet;
	}

	public DancingLinks(LinkedList<E> groundSet, LinkedList<Set<E>> collection) {
		super(groundSet, collection);
		reference = (LinkedList) collection;
		h = new colObj();
		dataObj last = h;
		for (E element : groundSet) {
			colObj<E> cur = new colObj<E>(element);
			cur.R = h;
			last.R = cur;
			cur.L = last;
			h.L = cur;
			cur.U = cur;
			cur.D = cur;
			cur.last = cur;
			cur.C = cur;
			last = cur;
		}
		int k = 0;
		for (Set<E> set : collection) {
			dataObj first = new dataObj();
			last = new dataObj();
			for (colObj<E> col = (colObj<E>) h.R; !col.equals(h); col = (colObj<E>) col.R) {
				if (set.contains(col.N)) {
					dataObj cur = new dataObj();
					cur.set = k;
					if (last.R == null) {
						cur.R = cur;
						cur.L = cur;
						first = cur;
						last = cur;
					} else {
						cur.R = first;
						first.L = cur;
						cur.L = last;
						last.R = cur;
					}
					cur.U = col.last;
					col.last.D = cur;
					cur.D = col;
					col.U = cur;
					cur.C = col;
					last = cur;
					col.last = cur;
					col.S++;

				}
			}
			k++;
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
			for (dataObj y = t.L; !y.equals(t); y = y.L) {
				coverColumn(y.C);
			}
			DancingLinks<E> dls = new DancingLinks<E>();
			dls.exactCover();
			for (Set<dataObj> P : dls.solution) {
				P.add(t);
				solution.add(P);
			}
			if (solution.size() >= 10)
				break;
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
		for (int i = 1; i <= 3; i++) {
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
		for (int i = 1; i <= 4; i++) {
			ECP.groundSet.add(i);
		}
		ECP.collection.addAll(ExactCover.subsets(new HashSet<Integer>(ECP.groundSet)));
//		ECP.collection.addAll(ExactCover.subsetsK(new HashSet<Integer>(ECP.groundSet), 2));
//		System.out.println(ECP.collection);

		// Randomly removing half of the subsets;
//		Random r = new Random();
//		int number = ECP.collection.size();
//		for (int i = 0; i < number/2; i++)
//			ECP.collection.remove(r.nextInt((number - i)));

//		ECP.Matrix();
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
		Test2();
	}
}
