package helpers;

public class Test {

	public static void main (String[] args) {
//		Span[] s1 = new Span[7];
//		Span[] s2 = new Span[6];
//		s1[0] = new Span('A');
//		s1[1] = new Span('C');
//		s1[2] = new Span('G');
//		s1[3] = new Span('G');
//		s1[4] = new Span('T');
//		s1[5] = new Span('A');
//		s1[6] = new Span('G');
//		
//		s2[0] = new Span('C');
//		s2[1] = new Span('C');
//		s2[2] = new Span('T');
//		s2[3] = new Span('A');
//		s2[4] = new Span('A');
//		s2[5] = new Span('G');
//		SpanAlignment sa = new SpanAlignment(s1, s2);
////////////////////////////////////////////////////////////////////////
		Span[] s1 = new Span[4];
		Span[] s2 = new Span[4];
		int[] a = new int[2];
		int[] b = {41,47,100,128,150,157}; s1[0] = new Span(b);
		a[0] = 100; a[1] = 128; s1[1] = new Span(a);
		a[0] = 116; a[1] = 128; s1[2] = new Span(a);
		a[0] = 150; a[1] = 157; s1[3] = new Span(a);
		int[] c = {41,49,100,128}; s2[0] = new Span(c);
		a[0] = 100; a[1] = 128; s2[1] = new Span(a);
		a[0] = 110; a[1] = 128; s2[2] = new Span(a);
		a[0] = 200; a[1] = 208; s2[3] = new Span(a);
		SpanAlignment sa = new SpanAlignment (s1, s2);

		int[] id = sa.get1();
		for (int i : id)
			System.out.print(i+" ");
		System.out.println();
		id = sa.get2();
		for (int i : id)
			System.out.print(i+" ");
		System.out.println();
	}
}
