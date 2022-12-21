package site.starsone.xtool.view.lpk;

public class Tuple2<T1, T2> {
    public static void main(String[] args) {
        String str = "a";
        int i = str.charAt(0) + 10;
        System.out.println(i);
    }
    private final T1 first;
    private final T2 second;

    protected Tuple2(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<>(t1, t2);
    }

    public T1 getFirst() {
        return this.first;
    }

    public T2 getSecond() {
        return this.second;
    }
}
