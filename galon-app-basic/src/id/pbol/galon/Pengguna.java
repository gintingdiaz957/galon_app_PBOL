package id.pbol.galon;

public class Pengguna {
    public enum Peran {
        KONSUMEN("Konsumen"), ADMIN("Admin"), PENGANTAR("Pengantar");

        private final String label;

        Peran(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final int id;
    private final String nama;
    private final Peran peran;

    public Pengguna(int id, String nama, Peran peran) {
        this.id = id;
        this.nama = nama;
        this.peran = peran;
    }

    public int getId() {
        return id;
    }

    public Peran getPeran() {
        return peran;
    }

    @Override
    public String toString() {
        return nama;
    }
}
