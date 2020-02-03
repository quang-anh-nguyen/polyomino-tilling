import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedList;

public class Test {

	static int N;
	static long executionTime;
	static int size = 20;
	static Dimension screen = new Dimension(1920, 1080);

	public static Color[] color = { Color.red, Color.green, Color.blue, Color.yellow, Color.cyan,
			Color.magenta, Color.orange, Color.gray, Color.pink, Color.darkGray, Color.lightGray };

	public static void getDimension(int length) {
		if (length > 10000)
			size = 10;
		if (length > 50000)
			size = 5;
		int nx, ny;
		if (size * length * (N + 2) < screen.width)
			screen = new Dimension(size * length * (N + 2), size * (N + 2));
		nx = screen.width / (size * (N + 2));
		ny = (int) Math.ceil((size * length * (N + 2) + 0.0) / (screen.width));
		if (ny * size * (N + 2) < screen.height)
			screen = new Dimension(nx * size * (N + 2), ny * size * (N + 2));
		else
			screen = new Dimension(nx * size * (N + 2), ny * size * (N + 2));
	}

	public static void draw(LinkedList<Polyomino> polys, String filename) {
		int width = screen.width;
		int height = screen.height;
		Image2d img = new Image2d(width, height);
		int c = 0;
		for (Polyomino P : polys) {
			P.addToImage(img, 2, size, color[c]);
			c++;
			c = c % color.length;
		}
		Image2dComponent imgcom = new Image2dComponent(img);
		Image2dViewer imgvr = new Image2dViewer(img);
		imgvr.getContentPane().add(imgcom);
		imgcom.saveImage("results/" + filename, width, height);
	}

	public static void demo(LinkedList<? extends Polyomino> polys, String filename) {
//		getDimension(polys.size());
		int width = screen.width;
		int height = screen.height;
		Image2d img = new Image2d(width, height);
		int offsetX = size;
		int offsetY = size;
		int c = 0;
		for (Polyomino P : polys) {
			P.addToShow(img, 2, size, offsetX, offsetY, Color.red);
			offsetX += size * (N + 2);
			if (offsetX > width - 0 * (N + 2) * size) {
				offsetY += size * (N + 2);
				offsetX = size;
			}
			c++;
			c = c % color.length;
		}
		Image2dComponent imgcom = new Image2dComponent(img);
		Image2dViewer imgvr = new Image2dViewer(img);
		imgvr.getContentPane().add(imgcom);
		imgcom.saveImage("results/generating" + filename, width, height);
	}

	public static void test1() {
		demo(Polyomino.PolyominoList("polyominoesINF421.txt"), "INF421.png");
	}

	public static void test2() {
//		String filename = "Naive_" + Integer.toString(N) + "_fixed" + ".png";
		String filename = "Naive_" + Integer.toString(N) + "_free" + ".png";

		long startTime = System.currentTimeMillis();

//		STARTING PROGRAM ---------------------------------------------------------------------------

//		LinkedList<Polyomino> list = Polyomino.generateFixedNaive(N);
		LinkedList<Polyomino> list = Polyomino.generateFreeNaive(N);

//		ENDING PROGRAM ---------------------------------------------------------------------------

		long endTime = System.currentTimeMillis();
		executionTime = endTime - startTime;

		draw(list, filename);

//		System.out.println("Number of fixed polyominos = " + list.size());
		System.out.println("Number of free polyominos = " + list.size());

		System.out.println("Execution time = " + executionTime + " ms");

	}

	public static void test3() {
//		String filename = "Redelmeier_" + Integer.toString(N) + "_fixed" + ".png";
//		String filename = "Redelmeier_" + Integer.toString(N) + "_free" + ".png";

		long startTime = System.currentTimeMillis();

//		STARTING PROGRAM ---------------------------------------------------------------------------
		PolyNode.N = N;

		LinkedList<? extends Polyomino> list = PolyNode.generateFixed();
//		LinkedList<? extends Polyomino> list = PolyNode.generateFree();

//		ENDING PROGRAM ---------------------------------------------------------------------------

		long endTime = System.currentTimeMillis();
		executionTime = endTime - startTime;

//		System.out.println("Number of fixed polyominos = " + list.size());
		System.out.println("Number of free polyominos = " + list.size());

//		demo(list, filename);

		System.out.println("Execution time = " + executionTime + " ms");

	}

	public static void main(String[] args) {
		N = 4;
		System.out.println("Number of cells = " + N);
		test1();  	//YES
//		test2();	//YES
//		test3();	//YES
		System.out.println("Done!!!...");
	}
}
