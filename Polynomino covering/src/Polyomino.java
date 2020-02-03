import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Polyomino {

	public LinkedList<Square> squares; // set of squares of the polyomino
	public int n; // area of polyomino
	public int xmin, xmax, ymin, ymax;
	public boolean[][] tiles;
	public Square base; // leftmost cell of the bottom row

	public static int[] limits(LinkedList<Square> squares) {
		if (squares.isEmpty())
			return null;
		Square s = squares.element();
		int[] limits = { s.x, s.x, s.y, s.y };
		for (Square sq : squares) {
			limits[0] = Math.min(limits[0], sq.x);
			limits[1] = Math.max(limits[1], sq.x);
			limits[2] = Math.min(limits[2], sq.y);
			limits[3] = Math.max(limits[3], sq.y);
		}
		return limits;
	}

	public Polyomino() {
		this.squares = new LinkedList<Square>();
		this.n = 0;
	}

	public static Square findBase(LinkedList<Square> squares) {
		if (squares.isEmpty())
			return null;
		Square sq = squares.element();
		for (Square s : squares) {
			if (s.y < sq.y)
				sq = s;
			if ((s.y == sq.y) && (s.x < sq.x))
				sq = s;
		}
		return sq;
	}

	public String toString() {
		String s = "[";
		for (Square sq : squares) {
			if (squares.get(0) != sq)
				s += ",";
			s += sq.toString();
		}
		s += "]";
		return s;
	}

	public Polyomino(LinkedList<Square> squares) {
		this.squares = squares;
		this.xmin = limits(squares)[0];
		this.xmax = limits(squares)[1];
		this.ymin = limits(squares)[2];
		this.ymax = limits(squares)[3];
		this.n = squares.size();
		this.base = findBase(squares);
		boolean[][] tiles = new boolean[xmax - xmin + 1][ymax - ymin + 1];
		for (Square sq : squares)
			tiles[sq.x - xmin][sq.y - ymin] = true;
		this.tiles = tiles;

	}

	public int width() {
		if (this.n == 0)
			return 0;
		return xmax - xmin + 1;
	}

	public int height() {
		if (this.n == 0)
			return 0;
		return ymax - ymin + 1;
	}

//	public Polyomino canonicalForm() {
//		return this.translation(-base.x, -base.y);
//	}

	public static LinkedList<Square> squareList(String s) {
		LinkedList<Square> squares = new LinkedList<Square>();
		int sign = 1;
		int x = 0;
		int y = 0;
		boolean reading_x = false;
		boolean reading_y = false;
		for (int i = 1; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '(') {
				reading_x = true;
				sign = 1;
				x = 0;
				y = 0;
			} else if ((c == ',') & reading_x) {
				reading_x = false;
				reading_y = true;
				sign = 1;
			} else if (c == ')') {
				reading_y = false;
				squares.add(new Square(x, y));
			} else if (c == '-')
				sign = -1;
			else if (c == ' ') {
			} else if (reading_x)
				x = x * 10 + sign * Integer.parseInt(Character.toString(c));
			else if (reading_y)
				y = y * 10 + sign * Integer.parseInt(Character.toString(c));
		}
		return squares;
	}

	public Polyomino(String s) {
		this(squareList(s));
	}

//	public LinkedList<Square> getSquares() {
//		return this.squares;
//	}

	public int getNum() {
		return this.n;
	}

	// Translation of this by a vector (deltaX, deltaY)

	public Polyomino translation(int deltaX, int deltaY) {
		LinkedList<Square> squares = new LinkedList<Square>();
		for (Square sq : this.squares) {
			squares.add(new Square(sq.x + deltaX, sq.y + deltaY));
		}
		return new Polyomino(squares);
	}

	// Equal of polyominos up to a translation

	public boolean equalsFixed(Polyomino P) {
		if (this.n != P.n)
			return false;
		if (this.width() != P.width())
			return false;
		if (this.height() != P.height())
			return false;
		if ((this.base.x - this.xmin != P.base.x - P.xmin))
			return false;
		for (int i = 0; i < this.width(); i++)
			for (int j = 0; j < this.height(); j++)
				if (this.tiles[i][j] != P.tiles[i][j])
					return false;
		return true;
	}

	// rotation of P counter-clockwise 90 degree

	public Polyomino rotation() {
		LinkedList<Square> squares = new LinkedList<Square>();
		for (Square sq : this.squares) {
			squares.add(new Square(-sq.y, sq.x));
		}
		return new Polyomino(squares);
	}

	// Equal up to a rotation and translation

	public boolean equalsOneSide(Polyomino P) {
		Polyomino PP = new Polyomino(P.squares);
		for (int i = 0; i < 4; i++) {
			if (this.equalsFixed(PP))
				return true;
			PP = PP.rotation();
		}
		return false;
	}

	// reflection of P with respect to axe y

	public Polyomino reflectionY() {
		LinkedList<Square> squares = new LinkedList<Square>();
		for (Square sq : this.squares) {
			squares.add(new Square(-sq.x, sq.y));
		}
		return new Polyomino(squares);
	}

	// Equal up to an isometry

	public boolean equalsFree(Polyomino P) {
		if (this.equalsOneSide(P))
			return true;
		Polyomino PP = P.reflectionY();
		if (this.equalsOneSide(PP))
			return true;
		return false;
	}

	public LinkedList<Polyomino> variants(boolean flip) {
		LinkedList<Polyomino> variants = new LinkedList<Polyomino>();
		Polyomino P = this;
		int I = 1;
		if (flip)
			I = 2;
		for (int i = 0; i < I; i++) {
			for (int j = 0; j < 4; j++) {
				if (P.inList(variants, true) < 0)
					variants.add(P);
				P = P.rotation();
			}
			if (P.equalsFixed(P.reflectionY()))
				break;
			P = P.reflectionY();
		}
		return variants;
	}

	// Check in list, fixe is true if polynomios are fixed, false if not

	public int inList(LinkedList<Polyomino> polys, boolean fixed) {
		if (fixed) {
			for (Polyomino P : polys)
				if (this.equalsFixed(P))
					return polys.indexOf(P);
			return -1;
		} else {
			for (Polyomino P : polys)
				if (this.equalsFree(P))
					return polys.indexOf(P);
			return -1;
		}

	}

	// Dilation of P with ratio k

	public Polyomino dilation(int k) {
		LinkedList<Square> sqs = new LinkedList<Square>();
		for (Square sq : squares) {
			for (int i = 0; i < k; i++)
				for (int j = 0; j < k; j++)
					sqs.add(new Square(k * sq.x + i, k * sq.y + j));
		}
		return new Polyomino(sqs);
	}

	static public LinkedList<Polyomino> PolyominoList(String filename) {
		LinkedList<Polyomino> polyominos = new LinkedList<Polyomino>();
		try {
			BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line;
			while ((line = buffer.readLine()) != null) {
				polyominos.add(new Polyomino(line));
			}
			buffer.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return polyominos;
	}

	public boolean contains(Square sq) {
		if (this.n == 0)
			return false;
		if ((sq.x < this.xmin) || (sq.x > this.xmax))
			return false;
		if ((sq.y < this.ymin) || (sq.y > this.ymax))
			return false;
		return this.tiles[sq.x - xmin][sq.y - ymin];
	}

	public Polyomino normalForm() {
		return translation(-xmin, -ymin);
	}

	public void addToShow(Image2d img, int width, int size, int beginX, int beginY, Color color) {
		Polyomino P = this.normalForm();
		int c = 0;
		for (Square sq : P.squares) {
			int offsetX = beginX;
			int offsetY = P.height() * size + beginY;
			int[] xcoords = { offsetX + sq.x * size, offsetX + (sq.x + 1) * size, offsetX + (sq.x + 1) * size,
					offsetX + sq.x * size };
			int[] ycoords = { offsetY - sq.y * size, offsetY - sq.y * size, offsetY - (sq.y + 1) * size,
					offsetY - (sq.y + 1) * size };
			img.addPolygon(xcoords, ycoords, Test.color[c % 10]);
			if (!P.contains(new Square(sq.x - 1, sq.y)))
				img.addEdge(offsetX + sq.x * size, offsetY - sq.y * size, offsetX + sq.x * size,
						offsetY - (sq.y + 1) * size, width);
			if (!P.contains(new Square(sq.x + 1, sq.y)))
				img.addEdge(offsetX + (sq.x + 1) * size, offsetY - sq.y * size, offsetX + (sq.x + 1) * size,
						offsetY - (sq.y + 1) * size, width);
			if (!P.contains(new Square(sq.x, sq.y + 1)))
				img.addEdge(offsetX + sq.x * size, offsetY - (sq.y + 1) * size, offsetX + (sq.x + 1) * size,
						offsetY - (sq.y + 1) * size, width);
			if (!P.contains(new Square(sq.x, sq.y - 1)))
				img.addEdge(offsetX + sq.x * size, offsetY - sq.y * size, offsetX + (sq.x + 1) * size,
						offsetY - sq.y * size, width);
//			c++;
		}
	}

	public void addToImage(Image2d img, int width, int size, Color color) {
		for (Square sq : this.squares) {
			int floor = img.getHeight();
			int[] xcoords = { sq.x * size, (sq.x + 1) * size, (sq.x + 1) * size, sq.x * size };
			int[] ycoords = { floor - sq.y * size, floor - sq.y * size, floor - (sq.y + 1) * size,
					floor - (sq.y + 1) * size };
			img.addPolygon(xcoords, ycoords, color);
			if (!this.contains(new Square(sq.x - 1, sq.y)))
				img.addEdge(sq.x * size, floor - sq.y * size, +sq.x * size, floor - (sq.y + 1) * size, width);
			if (!this.contains(new Square(sq.x + 1, sq.y)))
				img.addEdge(+(sq.x + 1) * size, floor - sq.y * size, +(sq.x + 1) * size, floor - (sq.y + 1) * size,
						width);
			if (!this.contains(new Square(sq.x, sq.y + 1)))
				img.addEdge(+sq.x * size, floor - (sq.y + 1) * size, +(sq.x + 1) * size, floor - (sq.y + 1) * size,
						width);
			if (!this.contains(new Square(sq.x, sq.y - 1)))
				img.addEdge(+sq.x * size, floor - sq.y * size, +(sq.x + 1) * size, floor - sq.y * size, width);
		}
	}

	public Polyomino addSquare(Square sq) {
		if (this.squares == null) {
			LinkedList<Square> squares = new LinkedList<Square>();
			squares.add(sq);
			return new Polyomino(squares);
		}
		if (this.contains(sq))
			return this;
		LinkedList<Square> squares = new LinkedList<Square>();
		squares.addAll(this.squares);
		squares.add(sq);
		return new Polyomino(squares);
	}

	public boolean isNeighbor(Square sq) {
		if (this.contains(sq))
			return false;
		for (Square s : sq.neighbors()) {
			if (this.contains(s))
				return true;
		}
		return false;
	}

	public LinkedList<Square> neighbors() {
		LinkedList<Square> list = new LinkedList<>();
		for (Square sq : this.squares)
			for (Square s : sq.neighbors())
				if (this.isNeighbor(s))
					list.add(s);
//		for (int i = this.xmin - 1; i <= this.xmax + 1; i++)
//			for (int j = this.ymin - 1; j <= this.ymax + 1; j++) {
//				Square s = new Square(i, j);
//				if (this.isNeighbor(s)) {
//					list.add(s);
//				}
//			}
		return list;
	}

	public LinkedList<Polyomino> addNeighbor(boolean fixed) {
		LinkedList<Polyomino> list = new LinkedList<Polyomino>();
		if (this.n == 0) {
			list.add(new Polyomino("[(0,0)]"));
			return list;
		}
		for (Square sq : this.neighbors()) {
			Polyomino p = this.addSquare(sq);
			if (p.inList(list, fixed) < 0)
				list.add(this.addSquare(sq));
		}
		return list;
	}

	public void print() {
		for (Square sq : this.squares)
			System.out.print(sq.toString() + " ");
		System.out.println();
	}

	public static LinkedList<Polyomino> generate(int n, boolean fixed) {
		LinkedList<Polyomino> list = new LinkedList<Polyomino>();
		if (n == 1) {
			list.add(new Polyomino("[(0,0)]"));
			return list;
		}
		for (Polyomino P : generate(n - 1, fixed)) {
			for (Polyomino p : P.addNeighbor(fixed)) {
				if (p.inList(list, fixed) < 0) {
					list.add(p);
				}
			}
		}
		return list;
	}

	public static LinkedList<Polyomino> generateFixedNaive(int n) {
		return generate(n, true);
	}

	public static LinkedList<Polyomino> generateFreeNaive(int n) {
		return generate(n, false);
	}

//	Here we use Square.getSquare(x, y) instead of new Square(x, y) so as that each square correspond to a unique object, listed in Square.source 
	public Set<Square> getSquares() {
		Set<Square> list = new HashSet<Square>();
		for (Square sq : this.squares) {
			list.add(Square.getSquare(sq.x, sq.y));
		}
		return list;
	}

	public boolean includes(Polyomino P) {
		if (P.xmin < xmin || P.xmax > xmax || P.ymin < ymin || P.ymax > ymax)
			return false;
		for (int i = 0; i < P.width(); i++) {
			for (int j = 0; j < P.height(); j++) {
				if (P.tiles[i][j] && !tiles[-xmin + P.xmin + i][-ymin + P.ymin + j])
					return false;
			}
		}
		return true;
	}

}
