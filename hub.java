public class hub {
    private double[][] base;
    private double[][] top_horizon_offset;
    private double[][] volume;
    private double[][] persentage;
    private double cell=150;
    private double fluidContact=2500;
    private String txt;
    public hub(){
        // to use method
        //
    }
    public hub(String str){
        this.txt = str;
        setthis();
    }
    public void set(String str){
        this.txt = str;
        setthis();
    }
    public void setfluidcontact(double fc) {
        this.fluidContact = fc;
        setthis();
    }
    private void setthis() {
        int[][] intbase = toinfoBase(txt);
        int[][] inttop = toinfoTop(intbase);
        base = todouble(intbase);
        top_horizon_offset = todouble(inttop);
        volume = tovolume(top_horizon_offset);
        persentage = topersent(top_horizon_offset);
    }
    private int[][] toinfoBase (String str) {
        String[] splitline = str.split("\n");  //sinificant
        String[] ins = str.split(" ");
        String str2 = str.replaceAll("\n", "");
        String[] ins2 = str2.split(" ");

        int n=splitline.length;

        int col=n;
        int row=(ins2.length)/n;
        System.out.println("col:"+col);
        System.out.println("row:"+row);
        int[][] infoA = new int[col][row];
        int count=0;
        for(int i=0;i<col;i=i+1){
            for(int j=0;j<row;j=j+1){
                infoA[i][j]=Integer.parseInt(ins2[count]);
                count=count+1;
                //System.out.println(infoA[i][j]+" "+count +"["+i+"]"+"["+j+"]");
            }
        }
        return infoA;
    }
    private int[][] toinfoTop (int[][] Ar){
        int[][] tophorizon = new int[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                tophorizon[i][j]=Ar[i][j]-200;
                //System.out.println(tophorizon[i][j]+" "+i+","+j);
            }
        }
        return tophorizon;
    }
    private double[][] todouble (int[][] Ar){
        double[][] Ard = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                Ard[i][j]=(float)(Ar[i][j]);
            }
        }
        return Ard;
    }
    private double[][] tovolume(double[][] Ar){
        double[][] Arv = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                if(base[i][j]>fluidContact && top_horizon_offset[i][j]<fluidContact){
                    Arv[i][j] = cell*cell*(fluidContact-Ar[i][j]);
                }
                else{
                    Arv[i][j] = 0;
                }
            }
        }
        return Arv;
    }
    private double[][] topersent(double[][] Ar){
        double[][] Arp = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                Arp[i][j] = ((fluidContact-Ar[i][j])/200)*100;
                if(Arp[i][j] > 100){
                    Arp[i][j] = 0;
                }
                else if(Arp[i][j] < 0){
                    Arp[i][j] = 0;
                }
            }
        }
        return Arp;
    }

    public double getfluidcontact() {
        return this.fluidContact;
    }
    public double[][]  getBase() {
        return this.base;
    }
    public double[][] getTop() {
        return this.top_horizon_offset;
    }
    public double getBase(int index1,int index2) {
        return this.base[index1][index2];
    }
    public double getTop(int index1,int index2) {
        return this.top_horizon_offset[index1][index2];
    }
    public double[][] getvolume(){
        return this.volume;
    }
    public double[][] getpersentage(){
        return this.persentage;
    }
    public double getvolume(int index1,int index2){
        return this.volume[index1][index2];
    }
    public double getpersentage(int index1,int index2){
        return this.persentage[index1][index2];
    }
    public String toString() {
        return "savadee kub";
    }

}

