import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class ExactCover<E> {

	public LinkedList<E> groundSet = new LinkedList<E>();
	public LinkedList<Set<E>> collection = new LinkedList<Set<E>>();
	public HashMap<Set<E>, HashMap<E, Boolean>> table = new HashMap<Set<E>, HashMap<E, Boolean>>();
	public Set<Set<Set<E>>> solution;

	public ExactCover(LinkedList<E> groundSet, LinkedList<Set<E>> collection,
			HashMap<Set<E>, HashMap<E, Boolean>> table) {
		this.groundSet = groundSet;
		this.collection = collection;
		this.table = table;
	}

	public ExactCover(LinkedList<E> groundSet, LinkedList<Set<E>> collection) {
		this.collection = collection;
		this.groundSet = groundSet;
		this.Matrix();
	}

	public ExactCover() {
		this.collection = new LinkedList<Set<E>>();
		this.groundSet = new LinkedList<E>();
	}

	public void printX() {
		System.out.println("X = " + groundSet);
	}

	public void printC() {
		System.out.println("C = " + collection);
	}

	public void Matrix() {
		for (Set<E> set : collection) {
			HashMap<E, Boolean> ind = new HashMap<E, Boolean>();
			table.put(set, ind);
			for (E element : groundSet) {
				ind.put(element, set.contains(element));
			}
		}
	}

	public void printM() {
		for (int i = 0; i < collection.size(); i++) {
			for (int j = 0; j < groundSet.size(); j++) {
				if (table.get(collection.get(i)).get(groundSet.get(j)))
					System.out.printf("%4d", 1);
				else
					System.out.printf("%4d", 0);
			}
			System.out.println();
		}
	}

//		Example given in project assignment
	
	public static void Test1() {
		ExactCover ECP = new ExactCover();
		for (int i = 1; i <= 7; i++) {
			ECP.groundSet.add(i);
		}
		ECP.collection = new LinkedList<Set<Integer>>();
		ECP.collection.add(new HashSet<>(Arrays.asList(new Integer[] { 3, 5, 6 })));
		ECP.collection.add(new HashSet<>(Arrays.asList(new Integer[] { 1, 4, 7 })));
		ECP.collection.add(new HashSet<>(Arrays.asList(new Integer[] { 2, 3, 6 })));
		ECP.collection.add(new HashSet<>(Arrays.asList(new Integer[] { 1, 4 })));
		ECP.collection.add(new HashSet<>(Arrays.asList(new Integer[] { 2, 7 })));
		ECP.collection.add(new HashSet<>(Arrays.asList(new Integer[] { 4, 5, 7 })));

		ECP.Matrix();
		ECP.printM();

		ECP.printX();
		ECP.printC();
		ECP.NaiveBackTracking();

		System.out.println("Solution: " + ECP.solution);
	}

//		Returning all subsets

	public static Set<Set<Integer>> subsets(Set<Integer> set) {
		Set<Set<Integer>> sset = new HashSet<Set<Integer>>();
		if (set.size() == 0) {
			sset.add(new HashSet<Integer>());
			return sset;
		}
		Integer x = set.toArray(new Integer[] {})[0];
		Set<Integer> u = new HashSet<Integer>();
		u.addAll(set);
		u.remove(x);
		for (Set<Integer> s : subsets(u)) {
			sset.add(s);
			Set<Integer> ss = new HashSet<Integer>();
			ss.addAll(s);
			ss.add(x);
			sset.add(ss);
		}
		return sset;
	}
	
	public static Set<Set<Integer>> subsetsK(Set<Integer> set, int k) {
		Set<Set<Integer>> sset = new HashSet<Set<Integer>>();
		if ((k == 0)){
			sset.add(new HashSet<Integer>());
			return sset;
		}
		if (k>set.size()) {
			return sset;
		}
		Integer x = set.toArray(new Integer[] {})[0];
		set.remove(x);
		sset.addAll(subsetsK(set, k));
		for (Set<Integer> s : subsetsK(set, k-1)) {
			s.add(x);
			sset.add(s);
		}
		set.add(x);
		return sset;
	}

//		 Covering from all subsets - and half of them

	public static void Test2() {
		ExactCover ECP = new ExactCover();
		for (int i = 1; i <= 10; i++) {
			ECP.groundSet.add(i);
		}
		ECP.collection.addAll(subsets(new HashSet<Integer>(ECP.groundSet)));
		
		// Randomly removing half of the subsets;
//		Random r = new Random();
//		int number = ECP.collection.size();
//		for (int i = 0; i < number/2; i++)
//			ECP.collection.remove(r.nextInt((number - i)));
		
		ECP.Matrix();
		ECP.printX();
//		ECP.printC();

		long startTime = System.currentTimeMillis();

		ECP.NaiveBackTracking();

		long endTime = System.currentTimeMillis();

		long executionTime = endTime - startTime;

		System.out.println("Execution time = " + executionTime + " ms");
		
		System.out.println("Solution: " + ECP.solution.size());

//		for (Set<Set<Integer>> sol : ECP.solution)
//			System.out.println(sol);
	}

//	Matrix Naive Backtracking

	public int colSum(E element) {
		int count = 0;
		for (Set<E> set : collection) {
			if (table.containsKey(set))
				if (table.get(set).containsKey(element))
					if (table.get(set).get(element))
						count++;
		}
		return count;
	}
	
	public E chooseX() {
		E x = groundSet.element();
		int count = collection.size();
		for (E element : groundSet)
			if (count > colSum(element)) {
				count = colSum(element);
				x = element;
			}
		return x;
	}

	public void NaiveBackTracking() {
//		System.out.println("X = " + groundSet + "    C = " + collection);
		solution = new HashSet<Set<Set<E>>>();
		if (groundSet.isEmpty()) {
			solution.add(new HashSet<Set<E>>());
			return;
		}
		E x = groundSet.element();
		x = chooseX();								// Test if choosing x contained in the least number of sets does improve speed
//		System.out.println("x = " + x);
		for (Set<E> S : collection) {
			if (S.contains(x)) {
				LinkedList<E> XX = (LinkedList<E>) groundSet.clone();
				LinkedList<Set<E>> CC = (LinkedList<Set<E>>) collection.clone();
				HashMap<Set<E>, HashMap<E, Boolean>> MM = (HashMap<Set<E>, HashMap<E, Boolean>>) table
						.clone();
//				System.out.println(MM);
//				System.out.println("S = " + S);
				for (E y : S) {
//					System.out.println("y = " + y);
					for (Set<E> T : collection) {
						if (!T.isEmpty()) {
							if (CC.contains(T)) {
//								System.out.println("T = " + T);
								if (T.contains(y)) {
									CC.remove(T);
									MM.remove(T);
								} else {
									MM.get(T).remove(y);
								}
							}
						}
					}
					XX.remove(y);
				}
				ExactCover<E> epc = new ExactCover<E>(XX, CC, MM);
				epc.NaiveBackTracking();
				for (Set<Set<E>> P : epc.solution) {
					P.add(S);
					solution.add(P);
				}
			}
		}
	}
	
	public static void Test3() {
		Set<Integer> set = new HashSet<Integer>(Arrays.asList(new Integer[] {1, 2, 3, 4}));
		System.out.println(subsetsK(set, 2));
	}
	

	public static void main(String[] args) {
//		Test1();
//		Test2();
		Test3();

	}

}

