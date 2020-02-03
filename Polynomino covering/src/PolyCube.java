import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

enum Lattice {
	CUBE, TRIANGLE, HEXAGON,
}

class PolyCubeTest {

	static int N = PolyCube.N;
	static int size = 5;
	static int line = 1;
	static Dimension screen = new Dimension(2400, 1800);
	public static Color[] color = { Color.white, Color.red, Color.green, Color.blue, Color.yellow, Color.cyan,
			Color.magenta, Color.orange, Color.gray, Color.pink, Color.darkGray, Color.lightGray };

	public static Color nextColor() {
		float h = (float) Math.random();
		return Color.getHSBColor(h, 1, 1);
	}

	public static void draw(LinkedList<PolyCube> polys, String filename, boolean real) {
		int c = 1;
		int left, floor;
		if (real) {
			floor = -YMIN;
			left = -XMIN;
			screen.width = XMAX - XMIN + 1;
			screen.height = YMAX - YMIN + 1;
		} else {
			left = size;
			floor = size;
			screen.height = (N + 1) * size;
			screen.width = N * size;
		}
		Image2d img = new Image2d(screen.width, screen.height);
		for (PolyCube P : polys) {
			int[] lim = PolyCubeTest.limits(P);
			P.addToImage(img, line, size, color[c], real, left, floor);
			c++;
			c = c % color.length;
			if (!real) {
				left += lim[1] - lim[0] + size;
				if (left > 10000) {
					floor += (N + 1) * size;
					screen.height += (N + 1) * size;
					img.height += (N + 1) * size;
					left = size;
				}
				screen.width = Math.max(left + size * N, screen.width);
				img.width = screen.width;
			}
		}
		Image2dComponent imgcom = new Image2dComponent(img);
		Image2dViewer imgvr = new Image2dViewer(img);
		imgvr.getContentPane().add(imgcom);
		imgcom.saveImage("results/" + filename, screen.width, screen.height);
	}

	public static LinkedList<PolyCube> drawTilling(Set<dataObj> os) {
		LinkedList<PolyCube> ps = new LinkedList<PolyCube>();
		for (dataObj o : os) {
			dataObj cur = o;
			PolyCube p = new PolyCube();
			while (true) {
				if (cur.C.N instanceof Cell) {
					p = p.addCube((Cell) cur.C.N);
				}
				cur = cur.R;
				if (cur.equals(o))
					break;
			}
			ps.add(p);
		}
		return ps;
	}

	static int XMIN, XMAX, YMIN, YMAX;

	static int min(int[] a) {
		int m = a[0];
		for (int x : a)
			if (x < m)
				m = x;
		return m;
	}

	static int max(int[] a) {
		int m = a[0];
		for (int x : a)
			if (x > m)
				m = x;
		return m;
	}

	public static int[] limits(PolyCube P) {
		int xmin = Cell.MAX * size;
		int xmax = -Cell.MAX * size;
		int ymin = Cell.MAX * size;
		int ymax = -Cell.MAX * size;
		for (Cell c : P.cells) {
			xmin = Math.min(xmin, min(c.getGeometry(0, 0, size)[0]));
			xmax = Math.max(xmax, max(c.getGeometry(0, 0, size)[0]));
			ymin = Math.min(ymin, min(c.getGeometry(0, 0, size)[1]));
			ymax = Math.max(ymax, max(c.getGeometry(0, 0, size)[1]));
		}
		return new int[] { xmin, xmax, ymin, ymax };
	}

	public static void frame(PolyCube P) {
		int[] lim = limits(P);
		XMIN = lim[0];
		XMAX = lim[1];
		YMIN = lim[2];
		YMAX = lim[3];
	}
}

abstract class Cell {
	public int x, y, z;
	public static int MAX = 30;

	public static Lattice lattice = PolyCube.lattice;

	public static int MAX(int x, int y, int z) {
		return Math.max(x, Math.max(y, z));
	}

	public int inList(LinkedList<Cell> list) {
		int k = 0;
		if (list == null)
			return -1;
		for (Cell cb : list) {
			if (equals(cb))
				return k;
			else
				k++;
		}
		return -1;
	}

	public String toString() {
		String s = "(";
		s += Integer.toString(x) + "," + Integer.toString(y) + "," + Integer.toString(z);
		s += ")";
		return s;
	}

	public boolean equals(Cell c) {
		return x == c.x & y == c.y & z == c.z;
	}

	public int compare(Cell cb) {
		if (z < cb.z)
			return -1;
		if (z > cb.z)
			return 1;
		if (y < cb.y)
			return -1;
		if (y > cb.y)
			return 1;
		if (x < cb.x)
			return -1;
		if (x > cb.x)
			return 1;
		return 0;
	}

	public abstract LinkedList<Cell> neighbors();

	public abstract int distance(Cell c);

	public abstract Cell transfrom(int i, int j, int k);

	int[] getCoordinates() {
		int[] coords = new int[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		return coords;
	}

	public static int[][] transformationCode;

	public static void getTransformations() {
		if (transformationCode != null)
			return;
		int[][] block = { { 1, 2, 3 }, { 1, 3, 2 }, { 2, 1, 3 }, { 2, 3, 1 }, { 3, 1, 2 }, { 3, 2, 1 } };
		if (lattice == Lattice.CUBE) {
			transformationCode = new int[48][3];
			for (int i = 0; i < 6; i++) {
				transformationCode[i][0] = block[i][0];
				transformationCode[i][1] = block[i][1];
				transformationCode[i][2] = block[i][2];

				transformationCode[i + 6][0] = -block[i][0];
				transformationCode[i + 6][1] = block[i][1];
				transformationCode[i + 6][2] = block[i][2];

				transformationCode[i + 12][0] = block[i][0];
				transformationCode[i + 12][1] = -block[i][1];
				transformationCode[i + 12][2] = block[i][2];

				transformationCode[i + 18][0] = -block[i][0];
				transformationCode[i + 18][1] = -block[i][1];
				transformationCode[i + 18][2] = block[i][2];

				transformationCode[i + 24][0] = block[i][0];
				transformationCode[i + 24][1] = block[i][1];
				transformationCode[i + 24][2] = -block[i][2];

				transformationCode[i + 30][0] = -block[i][0];
				transformationCode[i + 30][1] = block[i][1];
				transformationCode[i + 30][2] = -block[i][2];

				transformationCode[i + 36][0] = block[i][0];
				transformationCode[i + 36][1] = -block[i][1];
				transformationCode[i + 36][2] = -block[i][2];

				transformationCode[i + 42][0] = -block[i][0];
				transformationCode[i + 42][1] = -block[i][1];
				transformationCode[i + 42][2] = -block[i][2];
			}
			return;
		}
		if (lattice == Lattice.HEXAGON | lattice == Lattice.TRIANGLE) {
			transformationCode = new int[12][3];
			for (int i = 0; i < 6; i++) {
				transformationCode[i][0] = block[i][0];
				transformationCode[i][1] = block[i][1];
				transformationCode[i][2] = block[i][2];
				transformationCode[i + 6][0] = -block[i][0];
				transformationCode[i + 6][1] = -block[i][1];
				transformationCode[i + 6][2] = -block[i][2];
			}
			return;
		}

	}

	public static Cell getCell(int x, int y, int z) {
		if (lattice == Lattice.TRIANGLE)
			return Triangle.getTriangle(x, y, z);
		if (lattice == Lattice.HEXAGON)
			return Hexagon.getHexagon(x, y, z);
		else
			return Cube.getCube(x, y, z);
	}

	public Cell translate(int dX, int dY, int dZ) {
//		for TRIANGULAR and HEXAGONAL lattices, absolutely verify that dX+dY+dZ = 0
		if (lattice == Lattice.TRIANGLE)
			if (dX + dY + dZ == 0)
				return Triangle.getTriangle(x + dX, y + dY, z + dZ);
			else
				return null;
		if (lattice == Lattice.HEXAGON && dX + dY + dZ == 0)
			if (dX + dY + dZ == 0)
				return Hexagon.getHexagon(x + dX, y + dY, z + dZ);
			else
				return null;
		else
			return Cube.getCube(x + dX, y + dY, z + dZ);
	}

	abstract int[][] getGeometry(int floor, int left, int size);

}

class Cube extends Cell {

	private static Cell[][][] source;

	private Cube(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void initCube() {
		if (source != null)
			return;
		source = new Cube[2 * MAX][2 * MAX][2 * MAX];
		for (int i = 0; i < 2 * MAX; i++)
			for (int j = 0; j < 2 * MAX; j++)
				for (int k = 0; k < 2 * MAX; k++)
					source[i][j][k] = new Cube(i - MAX, j - MAX, k - MAX);
	}

	public static Cube getCube(int x, int y, int z) {
		if (Cell.MAX(Math.abs(x), Math.abs(y), Math.abs(z)) > MAX) {
			MAX = MAX * 2;
			source = null;
		}
		Cube.initCube();
		return (Cube) source[MAX + x][MAX + y][MAX + z];
	}

	@Override
	public LinkedList<Cell> neighbors() {
		LinkedList<Cell> list = new LinkedList<Cell>();
		list.add(getCube(x, y, z - 1));
		list.add(getCube(x, y - 1, z));
		list.add(getCube(x - 1, y, z));
		list.add(getCube(x + 1, y, z));
		list.add(getCube(x, y + 1, z));
		list.add(getCube(x, y, z + 1));
		return list;
	}

	public int distance(Cell c) {
		if (c instanceof Cube)
			return Math.abs(x - c.x) + Math.abs(y - c.y) + Math.abs(z - c.z);
		return -1;
	}

	@Override
	public Cube transfrom(int i, int j, int k) {
		int[] coords = getCoordinates();
		int X = coords[Math.abs(i) - 1] * (int) Math.signum(i);
		int Y = coords[Math.abs(j) - 1] * (int) Math.signum(j);
		int Z = coords[Math.abs(k) - 1] * (int) Math.signum(k);
		return getCube(X, Y, Z);
	}

	@Override
	public Cube translate(int dX, int dY, int dZ) {
		return getCube(x + dX, y + dY, z + dZ);
	}

	@Override
	int[][] getGeometry(int left, int floor, int size) {
		return null;
	}
}

class Triangle extends Cell {

	private static Cell[][][] source;

	int step;

	public static void initTriangle() {
		if (source != null)
			return;
		source = new Triangle[2 * MAX][2 * MAX][2 * MAX];
		for (int x = -MAX; x < MAX; x++)
			for (int y = -MAX; y < MAX; y++)
				if (Math.abs(x) + Math.abs(y) <= MAX) {
					int z = 0 - x - y;
					if (z >= -MAX & z < MAX & Math.abs(x) + Math.abs(y) + Math.abs(z) <= MAX) {
						source[x + MAX][y + MAX][z + MAX] = new Triangle(x, y, z);
					}
					z = 1 - x - y;
					if (z >= -MAX & z < MAX & Math.abs(x) + Math.abs(y) + Math.abs(z) <= MAX) {
						source[x + MAX][y + MAX][z + MAX] = new Triangle(x, y, z);
					}
				}

	}

	private Triangle(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.step = 1 - 2 * (x + y + z);
	}

	@Override
	public int distance(Cell tri) {
		if (tri instanceof Triangle)
			return Math.abs(x - tri.x) + Math.abs(y - tri.y) + Math.abs(z - tri.z);
		return -1;
	}

	@Override
	public LinkedList<Cell> neighbors() {
		LinkedList<Cell> list = new LinkedList<Cell>();
		list.add(getTriangle(x + step, y, z));
		list.add(getTriangle(x, y + step, z));
		list.add(getTriangle(x, y, z + step));
		return list;
	}

	public static Triangle getTriangle(int x, int y, int z) {

		if (Cell.MAX(Math.abs(x), Math.abs(y), Math.abs(z)) > MAX) {
			MAX = MAX * 2;
			source = null;
		}
		Triangle.initTriangle();
		return (Triangle) source[x + MAX][y + MAX][z + MAX];
	}

	@Override
	public Triangle transfrom(int i, int j, int k) {
//		i, j, k is a permutation of 1, 2, 3, indicating where 3 axes of a triangle are rearranged to transform it
		int[] coords = getCoordinates();
		int sign = (int) Math.signum(i);
		int X = coords[i * sign - 1] * sign;
		int Y = coords[j * sign - 1] * sign;
		int Z = (1 - sign) / 2 + coords[k * sign - 1] * sign;
		return getTriangle(X, Y, Z);
	}

	@Override
	int[][] getGeometry(int left, int floor, int size) {
		int[][] geo = new int[2][3];
		double alpha = Math.toRadians(120);
		double r = size / 1.5;
		double Y = (x - z) * Math.sqrt(3) / 2;
		double X = (x / 2. - y + z / 2.);
		for (int i = 0; i < 3; i++) {
			geo[1][i] = floor - (int) Math.round(r * (Y + step * Math.sin(i * alpha)));
			geo[0][i] = left - (int) Math.round(r * (X + step * Math.cos(i * alpha)));
		}
		return geo;
	}

	public static PolyCube parallelogram(int x, int y) {
		LinkedList list = new LinkedList();
		for (int i = 0; i <= x - 1; i++)
			for (int k = 1 - y; k <= 0; k++) {
				for (int j = -i - k; j <= 1 - i - k; j++) {
					if (j >= -MAX & j < MAX)
						list.add(getTriangle(i, j, k));
				}
			}
		return new PolyCube(list);
	}

}

class Hexagon extends Cell {

	private static Cell[][][] source;

	private Hexagon(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void initHexagon() {
		if (source != null)
			return;
		source = new Hexagon[2 * MAX][2 * MAX][2 * MAX];
		for (int x = -MAX; x < MAX; x++)
			for (int y = -MAX; y < MAX; y++) {
				int z = -x - y;
				if (z >= -MAX & z < MAX)
					source[x + MAX][y + MAX][z + MAX] = new Hexagon(x, y, z);
			}
	}

	public int distance(Hexagon hex) {
		return (Math.abs(x - hex.x) + Math.abs(y - hex.y) + Math.abs(z - hex.z)) / 2;
	}

	public LinkedList<Cell> neighbors() {
		LinkedList<Cell> list = new LinkedList<Cell>();
		list.add(new Hexagon(x + 1, y - 1, z));
		list.add(new Hexagon(x + 1, y, z - 1));
		list.add(new Hexagon(x, y + 1, z - 1));
		list.add(new Hexagon(x - 1, y + 1, z));
		list.add(new Hexagon(x - 1, y, z + 1));
		list.add(new Hexagon(x, y - 1, z + 1));
		return list;
	}

	public int distance(Cell hex) {
		if (hex instanceof Hexagon)
			return Math.abs(x - hex.x) + Math.abs(y - hex.y) + Math.abs(z - hex.z);
		return -1;
	}

	public static Hexagon getHexagon(int x, int y, int z) {
		if (Cell.MAX(Math.abs(x), Math.abs(y), Math.abs(z)) > MAX) {
			MAX = MAX * 2;
			source = null;
		}
		Hexagon.initHexagon();
		return (Hexagon) source[x + MAX][y + MAX][z + MAX];
	}

	@Override
	public Hexagon transfrom(int i, int j, int k) {
//	i, j, k is a permutation of 1, 2, 3 or -1, -2, -3, indicating how 6 axes of a hexagon are rearranged to transform it 
		int[] coords = getCoordinates();
		int sign = (int) Math.signum(i);
		int X = coords[i * sign - 1] * sign;
		int Y = coords[j * sign - 1] * sign;
		int Z = coords[k * sign - 1] * sign;
		return getHexagon(X, Y, Z);
	}

	@Override
	int[][] getGeometry(int left, int floor, int size) {
		int[][] geo = new int[2][6];
		double alpha = Math.toRadians(60);
		double r = size / 2.;
		double Y = (x + y / 2.) * Math.sqrt(3);
		double X = -y * 1.5;
		for (int i = 0; i < 6; i++) {
			geo[1][i] = floor - (int) Math.round(r * (Y + Math.sin(i * alpha)));
			geo[0][i] = left - (int) Math.round(r * (X + Math.cos(i * alpha)));
		}
		return geo;
	}

	public static PolyCube parallelogram(int x, int y) {
		LinkedList list = new LinkedList();
		for (int i = 0; i <= x - 1; i++)
			for (int k = 1 - y; k <= 0; k++) {
				int j = -k - i;
//				System.out.println(i + " " + j + " "+k);
				if (j >= -MAX & j < MAX)
					list.add(getHexagon(i, j, k));
			}
		return new PolyCube(list);
	}

}

public class PolyCube {

	public LinkedList<Cell> cells = new LinkedList<>();
	public int n;

	public PolyCube(LinkedList<Cell> cells) {
		this.cells = cells;
		this.n = cells.size();
	}

	public void addToImage(Image2d img, int width, int size, Color color, boolean real, int left, int floor) {
		for (Cell c : cells) {
			int[] lim = new int[4];
			if (!real) {
				lim = PolyCubeTest.limits(this);
			} else {
				lim = new int[] { 0, 0, 0, 0 };
			}
			int[][] coords = c.getGeometry(left - lim[0], floor - lim[2], size);
			img.addPolygon(coords[0], coords[1], color);
			for (int vertex = 0; vertex < coords[0].length; vertex++) {
				if (c.neighbors().get(vertex).inList(cells) < 0)
					img.addEdge(coords[0][vertex], coords[1][vertex], coords[0][(vertex + 1) % coords[0].length],
							coords[1][(vertex + 1) % coords[0].length], width);
			}
		}
	}

	public PolyCube(String s) {
		LinkedList<Cell> cells = new LinkedList<>();
		int sign = 1;
		int x = 0;
		int y = 0;
		int z = 0;
		boolean reading_x = false;
		boolean reading_y = false;
		boolean reading_z = false;
		for (int i = 1; i < s.length() - 1; i++) {
			char c = s.charAt(i);
			if (c == '(') {
				reading_x = true;
				sign = 1;
				x = 0;
				y = 0;
				z = 0;
			} else if ((c == ',') & reading_x) {
				reading_x = false;
				reading_y = true;
				sign = 1;
			} else if ((c == ',') & reading_y) {
				reading_y = false;
				reading_z = true;
				sign = 1;
			} else if (c == ')') {
				reading_z = false;
				if (lattice.equals(Lattice.TRIANGLE))
					cells.add(Triangle.getTriangle(x, y, z));
				else if (lattice.equals(Lattice.HEXAGON))
					cells.add(Hexagon.getHexagon(x, y, z));
				else if (lattice.equals(Lattice.CUBE))
					cells.add(Cube.getCube(x, y, z));
			} else if (c == '-')
				sign = -1;
			else if (c == ' ') {
			} else if (reading_x)
				x = x * 10 + sign * Integer.parseInt(Character.toString(c));
			else if (reading_y)
				y = y * 10 + sign * Integer.parseInt(Character.toString(c));
			else if (reading_z)
				z = z * 10 + sign * Integer.parseInt(Character.toString(c));
		}
		this.cells = cells;
		this.n = cells.size();
	}

	public boolean[][][] tiles;
	public int xmin, xmax, ymin, ymax, zmin, zmax;

	public void makeTiles() {
		if (tiles == null) {
			Cell first = cells.element();
			xmin = first.x;
			xmax = first.x;
			ymin = first.y;
			ymax = first.y;
			zmin = first.z;
			zmax = first.z;
			for (Cell c : cells) {
				xmin = Math.min(xmin, c.x);
				xmax = Math.max(xmax, c.x);
				ymin = Math.min(ymin, c.y);
				ymax = Math.max(ymax, c.y);
				zmin = Math.min(zmin, c.z);
				zmax = Math.max(zmax, c.z);
			}
			tiles = new boolean[xmax - xmin + 1][ymax - ymin + 1][zmax - zmin + 1];
			for (int x = xmin; x <= xmax; x++)
				for (int y = ymin; y <= ymax; y++)
					for (int z = zmin; z <= zmax; z++) {
						tiles[x - xmin][y - ymin][z - zmin] = false;
					}
			for (Cell c : cells) {
				tiles[c.x - xmin][c.y - ymin][c.z - zmin] = true;
			}
		}
	}

	public boolean equals(PolyCube P) {
		makeTiles();
		P.makeTiles();
		if (tiles.length != P.tiles.length)
			return false;
		if (tiles[0].length != P.tiles[0].length)
			return false;
		if (tiles[0][0].length != P.tiles[0][0].length)
			return false;
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				for (int k = 0; k < tiles[0][0].length; k++) {
					if (tiles[i][j][k] != P.tiles[i][j][k])
						return false;
				}
			}
		}
		return true;
	}

	public PolyCube transform(int i, int j, int k) {
		LinkedList<Cell> list = new LinkedList<Cell>();
		for (Cell c : cells) {
			list.add(c.transfrom(i, j, k));
		}
		return new PolyCube(list);
	}

	public int inList(LinkedList<PolyCube> polys, boolean rotate, boolean flip) {
		if (!rotate & !flip) {
			int k = 0;
			for (PolyCube P : polys)
				if (equals(P))
					return k;
				else
					k++;
			return -1;
		}
		for (PolyCube v : getVariants(rotate, flip)) {
			if (v.inList(polys, false, false) >= 0)
				return v.inList(polys, false, false);
		}
		return -1;
	}

	static int sign(int[] t) {
		if (lattice == Lattice.CUBE)
			return t[0] * t[1] * t[2] * (Math.abs(t[1]) - Math.abs(t[0])) * (Math.abs(t[2]) - Math.abs(t[1]))
					* (Math.abs(t[2]) - Math.abs(t[0]));
		else
			return (Math.abs(t[1]) - Math.abs(t[0])) * (Math.abs(t[2]) - Math.abs(t[1]))
					* (Math.abs(t[2]) - Math.abs(t[0]));
	}

	static boolean checkTransform(int[] transform, boolean rotate, boolean flip) {
		if (rotate & flip)
			return true;
		if (rotate & !flip) {
			return (sign(transform) > 0);
		} else
			return transform[0] == 1 & transform[1] == 2 & transform[2] == 3;
	}

	public LinkedList<PolyCube> getVariants(boolean rotate, boolean flip) {
		LinkedList<PolyCube> list = new LinkedList<PolyCube>();
		Cell.getTransformations();
		for (int[] transform : Cell.transformationCode) {
			if (checkTransform(transform, rotate, flip)) {
				PolyCube P = transform(transform[0], transform[1], transform[2]);
				if (P.inList(list, false, false) < 0)
					list.add(P);
			}

		}
		return list;
	}

	public LinkedList<Cell> neighbors = new LinkedList<Cell>();

	public LinkedList<Cell> getNeighbors() {
		if (neighbors.size() > 0)
			return neighbors;
		for (Cell c : cells) {
			for (Cell cc : c.neighbors()) {
				if (cc.inList(neighbors) < 0 & cc.inList(cells) < 0)
					neighbors.add(cc);
			}
		}
		return neighbors;
	}

	public PolyCube addCube(Cell c) {
		LinkedList<Cell> cbs = new LinkedList<Cell>();
		cbs.addAll(cells);
		cbs.add(c);
		return new PolyCube(cbs);
	}

	public static LinkedList<PolyCube> listN;

	public static Lattice lattice;

	public PolyCube() {
		this.n = 0;
		this.cells = new LinkedList<Cell>();
		this.untried = new LinkedList<Cell>();
		if (lattice.equals(Lattice.TRIANGLE)) {
			this.untried.add(Triangle.getTriangle(0, 0, 0));
			this.untried.add(Triangle.getTriangle(0, 0, 1));
		} else if (lattice == Lattice.HEXAGON) {
			this.untried.add(Hexagon.getHexagon(0, 0, 0));
		} else {
			this.untried.add(Cube.getCube(0, 0, 0));
		}

	}

	public boolean checkCell(Cell c) {
		if (this.n == 0)
			return true;
		if (c.compare(Cell.getCell(0, 0, 0)) <= 0)
			return false;
		if (c.inList(untried) >= 0)
			return false;
		if (c.inList(cells) >= 0)
			return false;
		return true;
	}

	public static PolyCube root;

	public String toString() {
		return cells.toString();
	}

	public LinkedList<Cell> untried = new LinkedList<Cell>();
	public LinkedList<Cell> tried = new LinkedList<Cell>();
	public LinkedList<PolyCube> children = new LinkedList<PolyCube>();

	public static int N;

	public static void generateFixed() {
		listN = new LinkedList<PolyCube>();
		root = new PolyCube();
		listN.add(root);
		while (listN.getFirst().n < N) {
			PolyCube parent = listN.pop();
			while (!parent.untried.isEmpty()) {
				Cell cb = parent.untried.pop();
				parent.tried.add(cb);
				PolyCube child = parent.addCube(cb);
				listN.addLast(child);
				parent.children.addLast(child);
				if (!(lattice == Lattice.TRIANGLE & child.n == 1
						& child.cells.getFirst().equals(Triangle.getTriangle(0, 0, 1))))
					if (child.n < N) {
						LinkedList<Cell> u = cb.neighbors();
						child.untried.addAll(parent.untried);
						child.tried.addAll(parent.tried);
						for (Cell c : u) {
							if (child.checkCell(c) & c.inList(child.tried) < 0) {
								child.untried.add(c);
							}
						}
					}
			}
		}
	}

	public static PolyCube search(PolyCube P) {
		Cell b = P.getBase();
		P = P.translate(-b.x, -b.y, -b.z);
		LinkedList<Cell> list = new LinkedList<Cell>();
		list.addAll(P.cells);
		PolyCube cur = root;
		while (true) {
			if (cur.n == P.n)
				return cur;
			for (PolyCube child : cur.children) {
				Cell c = child.cells.getLast();
				int k = c.inList(list);
				if (k >= 0) {
					list.remove(k);
					cur = child;
					break;
				}
			}
		}
	}

	public static void generateFree(boolean rotate, boolean flip) {
		generateFixed();
		LinkedList<PolyCube> list = new LinkedList<PolyCube>();
		while (!listN.isEmpty()) {
			PolyCube P = listN.pop();
			list.add(P);
			for (PolyCube p : P.getVariants(rotate, flip)) {
				PolyCube leaf = search(p);
				if (leaf != null)
					listN.remove(leaf);
			}
		}
		listN = list;
	}

	public Cell base;

	public Cell getBase() {
		if (base != null)
			return base;
		makeTiles();
		for (int y = ymin; y <= ymax; y++) {
			for (int x = xmin; x <= xmax; x++) {
				if (tiles[x - xmin][y - ymin][0]) {
					base = Cell.getCell(x, y, zmin);
					return base;
				}
			}
		}
		return base;
	}

	public static PolyCube box(int x, int y, int z) {
		LinkedList<Cell> list = new LinkedList<Cell>();
		for (int i = 0; i < x; i++)
			for (int j = 0; j < y; j++)
				for (int k = 0; k < z; k++) {
					if (Cell.getCell(i, j, k) != null)
						list.add(Cell.getCell(i, j, k));
				}
		return new PolyCube(list);
	}

	public boolean includes(PolyCube P) {
		for (Cell c : P.cells)
			if (c.inList(cells) < 0)
				return false;
		return true;
	}

	public PolyCube translate(int dX, int dY, int dZ) {
		LinkedList<Cell> list = new LinkedList<Cell>();
		for (Cell c : cells)
			if (c.translate(dX, dY, dZ) != null)
				list.add(c.translate(dX, dY, dZ));
		return new PolyCube(list);
	}

	public static LinkedList<Set> tryPos(PolyCube P, PolyCube p, boolean rotate, boolean flip) {
		LinkedList<Set> set = new LinkedList<Set>();
		for (PolyCube pp : p.getVariants(rotate, flip)) {
			Cell b = pp.getBase();
			for (Cell c : P.cells) {
				PolyCube ppp = pp.translate(c.x - b.x, c.y - b.y, c.z - b.z);
				if (ppp.cells.size() > 0)
					if (P.includes(ppp)) {
						Set s = new HashSet();
						s.addAll(ppp.cells);
						s.add(p); // for tilling without repeating
						set.add(s);
					}
			}
		}
		return set;
	}

	public static DancingLinks TillingPolyCube(PolyCube P, LinkedList<PolyCube> polys, boolean rotate, boolean flip) {
		if (lattice != Lattice.CUBE)
			PolyCubeTest.frame(P);
		LinkedList groundSet = new LinkedList(P.cells);
		groundSet.addAll(polys); // for tilling without repeating

		LinkedList<Set> collection = new LinkedList();
		for (PolyCube p : polys) {
			collection.addAll(tryPos(P, p, rotate, flip));
		}
		System.out.println("Size of collection = " + collection.size());
		DancingLinks DCL = new DancingLinks(groundSet, collection);
		return DCL;
	}

	public static void test1() {
		lattice = Lattice.CUBE;
		N = 4;
		PolyCube P = box(2, 2, 7);
		generateFree(true, true);

//		N = 7;
//		PolyCube P = new PolyCube("[(0,0,0),(0,1,0),(0,2,0),(1,0,0),(1,1,0),(2,0,0)]");
//		generateFixed();
		
//		N = 8;
//		generateFree(true, true);
//		System.out.println("Number of tilings = " + listN.size());

		DancingLinks problem = TillingPolyCube(P, listN, true, true);
		problem.exactCover();
		System.out.println(problem.solution.size());
	}

	public static void test2(int which) {
		lattice = Lattice.TRIANGLE;

		N = 9;
		System.out.println("Lattice = " + lattice);
		System.out.println("Number of cells = " + N);
		PolyCube P = new PolyCube("[(0,0,0)]");
		PolyCubeTest.size = 50;
		PolyCubeTest.line = 2;

		if (which == 0) {
			generateFixed();
			System.out.println("Number of fixed polyominoes = " + listN.size());
			endTime = System.currentTimeMillis();
			PolyCubeTest.draw(listN, "triangular_animals/" + N + "_fixed" + ".png", false);
		} else if (which == 1) {
			listN = null;
			generateFree(true, false);
			PolyCubeTest.draw(listN, "triangular_animals/" + N + "_oneside" + ".png", false);
			endTime = System.currentTimeMillis();
			System.out.println("Number of oneside polyominoes = " + listN.size());
		} else {
			listN = null;
			generateFree(true, true);
			PolyCubeTest.draw(listN, "triangular_animals/" + N + "_free" + ".png", false);
			endTime = System.currentTimeMillis();
			System.out.println("Number of free polyominoes = " + listN.size());
		}
	}

	public static void test3(int which) {
		lattice = Lattice.HEXAGON;

		N = 9;
		System.out.println("Lattice = " + lattice);
		System.out.println("Number of cells = " + N);
		PolyCubeTest.size = 50;
		PolyCubeTest.line = 2;

		if (which == 0) {
			generateFixed();
			System.out.println("Number of fixed polyominoes = " + listN.size());
			endTime = System.currentTimeMillis();
//			PolyCubeTest.draw(listN, "hexagonal_animals/" + N + "_fixed" + ".png", false);
		} else if (which == 1) {
			generateFree(true, false);
//			PolyCubeTest.draw(listN, "hexagonal_animals/" + N + "_oneside" + ".png", false);
			endTime = System.currentTimeMillis();
			System.out.println("Number of oneside polyominoes = " + listN.size());
		} else {
			generateFree(true, true);
//			PolyCubeTest.draw(listN, "hexagonal_animals/" + N + "_free" + ".png", false);
			endTime = System.currentTimeMillis();
			System.out.println("Number of free polyominoes = " + listN.size());
		}
	}

	public static PolyCube snowflake() {
		lattice = Lattice.TRIANGLE;
		return new PolyCube("[(1,0,0),(0,0,0),(0,1,0),"
				+ "(3,-1,-1),(2,-1,-1),(2,0,-1),(1,0,-1),(1,1,-1),(0,1,-1),(0,2,-1),(-1,2,-1),(-1,3,-1),"
				+ "(5,-2,-2),(4,-2,-2),(4,-1,-2),(3,-1,-2),(3,0,-2),(2,0,-2),(2,1,-2),(1,1,-2),(1,2,-2),(0,2,-2),(0,3,-2),(-1,3,-2),(-1,4,-2),(-2,4,-2),(-2,5,-2)"
				+ "(5,-2,-3),(5,-1,-3),(4,-1,-3),(4,0,-3),(3,0,-3),(3,1,-3),(2,1,-3),(2,2,-3),(1,2,-3),(1,3,-3),(0,3,-3),(0,4,-3),(-1,4,-3),(-1,5,-3),(-2,5,-3)"
				+ "(6,-1,-4),(5,-1,-4),(5,0,-4),(4,0,-4),(4,1,-4),(3,1,-4),(3,2,-4),(2,2,-4),(2,3,-4),(1,3,-4),(1,4,-4),(0,4,-4),(0,5,-4),(-1,5,-4),(-1,6,-4)"
				+ " (6,-1,-5),(6,0,-5),(5,0,-5),(5,1,-5),(4,1,-5),(4,2,-5),(3,2,-5),(3,3,-5),(2,3,-5),(2,4,-5),(1,4,-5),(1,5,-5),(0,5,-5),(0,6,-5)(-1,6,-5)"
				+ " (7,0,-6),(6,0,-6),(6,1,-6),(5,1,-6),(5,2,-6),(4,2,-6),(4,3,-6),(3,3,-6),(3,4,-6),(2,4,-6),(2,5,-6),(1,5,-6),(1,6,-6),(0,6,-6),(0,7,-6)"
				+ " (7,0,-7),(7,1,-7),(6,1,-7),(6,2,-7),(5,2,-7),(5,3,-7),(4,3,-7),(4,4,-7),(3,4,-7),(3,5,-7),(2,5,-7),(2,6,-7),(1,6,-7),(1,7,-7),(0,7,-7)"
				+ " (6,2,-8),(6,3,-8),(5,3,-8),(5,4,-8),(4,4,-8),(4,5,-8),(3,5,-8),(3,6,-8),(2,6,-8)"
				+ " (5,4,-9),(5,5,-9),(4,5,-9)]");
	}

	public static void test4(int which) {
		lattice = Lattice.TRIANGLE;
		int l1, l2;
		DancingLinks problem;

		if (which == 0) {
			PolyCubeTest.size = 100;
			PolyCubeTest.line = 5;
			N = 3;
			l1 = 3;
			l2 = 3;
			PolyCube P = Triangle.parallelogram(l1, l2);
			generateFixed();
			problem = TillingPolyCube(P, listN, false, false);
		} else if (which == 1) {
			PolyCubeTest.size = 50;
			PolyCubeTest.line = 3;
			N = 6;
			l1 = 6;
			l2 = 6;
			PolyCube P = Triangle.parallelogram(l1, l2);
			generateFree(true, true);
			System.out.println(listN.size());
			problem = TillingPolyCube(P, listN, true, true);
		} else {
			PolyCubeTest.size = 50;
			PolyCubeTest.line = 2;
			N = 6;
			PolyCube P = snowflake();
			generateFree(true, false);
			System.out.println(listN.size());
			problem = TillingPolyCube(P, listN, true, false);
		}

		problem.exactCover();
		System.out.println("Number of tillings = " + problem.solution.size());

		int count = 0;
		for (Set<dataObj> tile : (Set<Set<dataObj>>) problem.solution) {
			PolyCubeTest.draw(PolyCubeTest.drawTilling(tile),
//					"triangle_tilling/" + l1 + "," + l2 + "_" + N + "_" + (count + 1) + ".png", true);
					"triangle_tilling/snow_" + (count + 1) + ".png", true);
			count++;
		}
	}

	public static void test5(int which) {
		startTime = System.currentTimeMillis();
		lattice = Lattice.HEXAGON;
		int l1, l2;
		DancingLinks problem;
		PolyCubeTest.size = 50;
		PolyCubeTest.line = 5;

		if (which == 0) {
			N = 4;
			l1 = 16;
			l2 = 11;
			PolyCube P = Hexagon.parallelogram(l1, l2);
			generateFixed();
			System.out.println(listN.size());
			problem = TillingPolyCube(P, listN, false, false);

		} else if (which == 1) {
			N = 5;
			l1 = 11;
			l2 = 10;
			PolyCube P = Hexagon.parallelogram(l1, l2);
			generateFree(true, true);
			System.out.println(listN.size());
			problem = TillingPolyCube(P, listN, true, true);
		} else {
			N = 5;
			l1 = 11;
			l2 = 15;
			PolyCube P = Hexagon.parallelogram(l1, l2);
			generateFree(true, false);
			System.out.println(listN.size());
			problem = TillingPolyCube(P, listN, true, false);
		}

		problem.exactCover();
		System.out.println("Number of tillings = " + problem.solution.size());
		endTime = System.currentTimeMillis();

		int count = 0;
		for (Set<dataObj> tile : (Set<Set<dataObj>>) problem.solution) {

			PolyCubeTest.draw(PolyCubeTest.drawTilling(tile),
					"hexagon_tilling/" + l1 + "," + l2 + "_" + N + "_" + (count + 1) + ".png", true);
			count++;
		}
	}

	public static void test() {
		lattice = Lattice.TRIANGLE;
		N = 1;
		generateFree(true, false);
		System.out.println(listN.size());
	}

	public static long startTime, endTime, executionTime;

	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
//		------------------------------------------------------------------
//		test1(); // OK
//		test2();

//		test2(0);
//		test2(1);
//		test2(2);

//		test3(0);
//		test3(1);
//		test3(2);

//		test4(0);
//		test4(1);
//		test4(2);

//		test5(0);
//		test5(1);
		test5(2);

//		test();
//		------------------------------------------------------------------
		endTime = System.currentTimeMillis();
		executionTime = endTime - startTime;
		System.out.println("Execution time = " + executionTime + " ms");
	}

}
