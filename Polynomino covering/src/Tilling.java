import java.awt.Dimension;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Tilling extends ExactCover {

	public Polyomino ground;
	public Set<Polyomino> tiles;

	public Set<Set> tryPosFixed(Polyomino P, boolean rotation, boolean symetry, boolean reuse, Polyomino org) {
//		System.out.println("seed" + P);
		Set<Set> list = new HashSet<Set>();
		if (!symetry || !rotation) {
			for (Square sq : ground.squares) {
//					System.out.println("trying");
				Polyomino PP = P.translation(sq.x - P.base.x, sq.y - P.base.y);
				if (ground.includes(PP)) {
					Set s = PP.getSquares();
//					System.out.print(s);
					list.add(s);
					if (!reuse)
						s.add(org);
				}
			}
		} else {
			for (Polyomino p : P.variants(symetry))
				list.addAll(tryPosFixed(p, false, false, reuse, P));
		}
		return list;
	}

	public LinkedList<Set<Set<Square>>> tryPos(Set<Polyomino> tiles, boolean rotation, boolean symetry, boolean reuse) {
		LinkedList<Set<Set<Square>>> list = new LinkedList<Set<Set<Square>>>();
		for (Polyomino p : tiles) {
			list.add(new HashSet<>(tryPosFixed(p, rotation, symetry, reuse, p)));
		}
		return list;
	}

//	public LinkedList<Set<Set<Square>>> getCollection(Set<Polyomino> tiles, boolean rotation, boolean symetry, boolean reuse)

	public Tilling(Polyomino P, Set<Polyomino> S, boolean rotation, boolean symetry, boolean reuse) {
		super();
		Square.init();
		this.ground = P;
		this.tiles = S;
		this.groundSet.addAll(P.getSquares());
		for (Set<Set<Square>> list : tryPos(tiles, rotation, symetry, reuse))
			this.collection.addAll(list);
		if (!reuse) {
			groundSet.addAll(S);
		}
	}
	
	public void preview() {
		int c = 0;
		for (Set s : (LinkedList<Set>) collection) {
			Polyomino ppp = new Polyomino();
			for (Object o : s) {
				if ((o instanceof Square)) {
					ppp = ppp.addSquare((Square) o);
				}
			}
			LinkedList<Polyomino> l = new LinkedList<Polyomino>();
			l.add(ground);
			l.add(ppp);
			Test.draw(l, "hohoho.png");
			c++;
			if (c < 0)
				break;

		}
	}

	public static String figure51() {
		String s = "[";
		for (int i = 0; i <= 10; i++)
			for (int j = 5 - (i + 1) / 2; j <= 9 - i / 2; j++) {
				s += "(" + i + "," + j + ")";
				if (!(i == 10 && j == 4))
					s += ",";
			}
		s += "]";
		return s;
	}

	public static String figure52() {
		String s = "[";
		for (int i = 0; i <= 9; i++)
			for (int j = 0; j <= 10 - Math.abs(2 * i - 9); j++) {
				s += "(" + i + "," + j + ")";
				if (!(i == 10 && j == 4))
					s += ",";
			}
		s += "]";
		return s;
	}

	public static String figure53() {
		String s = "[";
		for (int i = 0; i <= 9; i++)
			for (int j = (int) (Math.abs(i - 4.5) - 0.5); j <= (int) (9.5 - Math.abs(i - 4.5)); j++) {
				s += "(" + i + "," + j + ")";
				if (!(i == 10 && j == 4))
					s += ",";
			}
		s += "]";
		return s;
	}

	public static void test1(int which) {
		String str[] = new String[3];
		str[0] = figure51();
		str[1] = figure52();
		str[2] = figure53();

		Polyomino P = new Polyomino(str[which - 1]);
		PolyNode.N = 5;
		LinkedList<Polyomino> list = PolyNode.generateFree();
		Set<Polyomino> S = new HashSet<Polyomino>(list);
		Tilling problem = new Tilling(P, S, true, true, false);
		Test.size = 30;
		Test.screen = new Dimension((P.width()) * Test.size, (P.height()) * Test.size);
		Test.N = 5;
//		problem.preview();
		long startTime = System.currentTimeMillis();
//		-------------------------------------------------------------------------------------------------------------
		System.out.println("Number of ways to put a tile = " + problem.collection.size());
		DancingLinks<Object> dcl = new DancingLinks(problem.groundSet, problem.collection);
		dcl.exactCover();
		System.out.println("Number of ways to tile = " + dcl.solution.size());
//		-------------------------------------------------------------------------------------------------------------
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		System.out.println("Execution time = " + executionTime + "ms");

		int c = 0;
//		System.out.println(solution.size());
		for (Set<dataObj> sol : dcl.solution) {
			LinkedList<Polyomino> Ps = new LinkedList<Polyomino>();
			Ps.add(P);
			for (dataObj o : sol) {
				dataObj cur = o;
				Polyomino ppp = new Polyomino();
				while (true) {
					if (cur.C.N instanceof Square) {
						ppp = ppp.addSquare((Square) cur.C.N);
					}
					cur = cur.R;
					if (cur.equals(o))
						break;
				}
				Ps.add(ppp);
			}
			c++;
			Test.draw(Ps, "ABC/" + which + "_" + Integer.toString(c) + ".png");
		}

	}

	public static void test() {
		Polyomino P = new Polyomino("[(0,0),(0,1),(0,2),(1,0),(1,1),(2,0)]");
		PolyNode.N = 3;
		Test.size = 100;
		Test.screen = new Dimension((P.width()) * Test.size, (P.height()) * Test.size);
		Test.N = 3;
		Set<Polyomino> l = new HashSet(PolyNode.generateFree());
//		System.out.println(l);
		Tilling t = new Tilling(P, l, true, true, true);
		System.out.println(t.collection.size());
//		t.preview();

		DancingLinks<Object> dcl = new DancingLinks(t.groundSet, t.collection);
		dcl.exactCover();
		
		int c = 0;
		System.out.println(dcl.solution.size());
		for (Set<dataObj> sol : dcl.solution) {
			LinkedList<Polyomino> Ps = new LinkedList<Polyomino>();
			Ps.add(P);
			for (dataObj o : sol) {
				dataObj cur = o;
				Polyomino ppp = new Polyomino();
				while (true) {
					if (cur.C.N instanceof Square) {
						ppp = ppp.addSquare((Square) cur.C.N);
					}
					cur = cur.R;
					if (cur.equals(o))
						break;
				}
				Ps.add(ppp);
			}
			c++;
			Test.draw(Ps, "" + Integer.toString(c) + ".png");
		}

	}

	public static String rectangle(int width, int height) {
		String s = "[";
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++) {
				s += "(" + Integer.toString(i) + "," + Integer.toString(j) + ")";
				if (i != 0 || j != 0)
					s += ",";
			}
		s += "]";
		return s;
	}

	public static void test2() {
		int n = 5;
		int width = 3;
		int height = 20;
		PolyNode.N = n;
		Test.N = n;
		Test.size = 50;
		Polyomino P = new Polyomino(rectangle(width, height));
		Test.screen = new Dimension((P.width()) * Test.size, (P.height()) * Test.size);

//		Set<Polyomino> list = new HashSet<Polyomino>(PolyNode.generateFixed());
		Set<Polyomino> list = new HashSet<Polyomino>(PolyNode.generateFree());

		Tilling problem = new Tilling(P, list, true, true, false); // modify here

		long startTime = System.currentTimeMillis();
//		-------------------------------------------------------------------------------------------------------------
		System.out.println("Number of ways to put a tile = " + problem.collection.size());
		DancingLinks<Object> dcl = new DancingLinks(problem.groundSet, problem.collection);
		dcl.exactCover();
		System.out.println("Number of ways to tile = " + dcl.solution.size());
//		-------------------------------------------------------------------------------------------------------------
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		System.out.println("Execution time = " + executionTime + "ms");

//		int c = 0;
//		for (Set<dataObj> sol : dcl.solution) {
//			LinkedList<Polyomino> Ps = new LinkedList<Polyomino>();
//			Ps.add(P);
//			for (dataObj o : sol) {
//				dataObj cur = o;
//				Polyomino ppp = new Polyomino();
//				while (true) {
//					if (cur.C.N instanceof Square) {
//						ppp = ppp.addSquare((Square) cur.C.N);
//					}
//					cur = cur.R;
//					if (cur.equals(o))
//						break;
//				}
//				Ps.add(ppp);
//			}
//			c++;
//			Test.draw(Ps, "DEF/" + Integer.toString(width) + "_" + Integer.toString(height) + "_" + Integer.toString(c)
//					+ ".png");
//		}
	}

	public static boolean dilateTilling(Polyomino P, int k) {
		Test.screen = new Dimension(P.width() * k * Test.size, P.height() * k * Test.size);
		Polyomino PP = P.translation(-P.xmin, -P.ymin);
		Polyomino kPP = PP.dilation(k);
		Set<Polyomino> l = new HashSet<Polyomino>();
		l.add(PP);
		Tilling problem = new Tilling(kPP, l, true, true, true);
		DancingLinks<Polyomino> dcl = new DancingLinks(problem.groundSet, problem.collection);
		dcl.exactCover();

		for (Set<dataObj> sol : dcl.solution) {
			LinkedList<Polyomino> Ps = new LinkedList<Polyomino>();
			Ps.add(kPP);
			for (dataObj o : sol) {
				dataObj cur = o;
				Polyomino ppp = new Polyomino();
				while (true) {
					if (cur.C.N instanceof Square) {
						ppp = ppp.addSquare((Square) cur.C.N);
					}
					cur = cur.R;
					if (cur.equals(o))
						break;
				}
				Ps.add(ppp);
			}
			Test.draw(Ps, "(" + P.getNum() + "," + k + ")" + "_" + P.toString() + ".png");
			break;
		}
		if (dcl.solution.isEmpty())
			return false;
		else
			return true;
	}

	public static void test3(int n, int k) {
		PolyNode.N = n;
		Test.N = n;
		Test.size = 30;

		long startTime = System.currentTimeMillis();
//		-------------------------------------------------------------------------------------------------------------
		Set<Polyomino> list = new HashSet<Polyomino>(PolyNode.generateFree());
		int count = 0;
		for (Polyomino P : list) {
			if (dilateTilling(P, k))
				count++;
		}
//		-------------------------------------------------------------------------------------------------------------
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		System.out.println("Number of tillable polyominoes = " + count);
		System.out.println("Execution time = " + executionTime + "ms");
	}

	public static void main(String[] args) {
//		test1(1);
//		test2();
		test3(8, 4);
//		test();
	}
}
