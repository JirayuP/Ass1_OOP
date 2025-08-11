public class hub {
    private double[][] base;                //getter method
    private double[][] top_horizon_offset;  //getter method
    private double[][] volume;              //getter method
    private double[][] persentage;          //getter method
    private double cell=150;
    private double fluidContact=2500;       //set 2500 is defult
    private String txt;                     //txt (data of this class/Object)
    public hub(){
        // to use method
    }
    public hub(String str){ //set object (constructor)
        this.txt = str;
        setthis();  // set object
    }
    public void set(String str){  //set object (by method)
        this.txt = str;
        setthis();  // set object
    }
    public void setfluidcontact(double newinput) { //set object (by method)
        this.fluidContact = newinput;
        setthis(); //set fluidcontact and update all attribute
    }
    private void setthis() {
        int[][] intbase = toinfoBase(txt);         //to base
        int[][] inttop = toinfoTop(intbase);       //turn base into Top
        base = todouble(intbase);                  //to double and set this object
        top_horizon_offset = todouble(inttop);     //to double and set this object
        volume = tovolume(top_horizon_offset);
        persentage = topersent(top_horizon_offset);
    }
    private int[][] toinfoBase (String str) {
        String[] splitline = str.split("\n");  // count line ( .length ) by "\n"
        String str2 = str.replaceAll("\n", " ");
        String[] ins2 = str2.split(" ");

        int n=splitline.length;    // count line

        int col=n;
        int row=(ins2.length)/n;
        System.out.println("col:"+col);  // test output
        System.out.println("row:"+row);  // test output
        int[][] infoA_int = new int[col][row];
        int count=0;    //count
        for(int i=0;i<col;i=i+1){
            for(int j=0;j<row;j=j+1){
                infoA_int[i][j]=Integer.parseInt(ins2[count]);
                count=count+1;  //count
            }
        }
        return infoA_int;
    }
    private int[][] toinfoTop (int[][] Ar){
        int[][] tophorizon = new int[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){   // Ar = array base
                tophorizon[i][j]=Ar[i][j]-200;   // top = base-200
            }
        }
        return tophorizon;
    }
    private double[][] todouble (int[][] Arrayint){
        double[][] Arrraydouble = new double[Arrayint.length][Arrayint[0].length];
        for(int i=0;i<Arrayint.length;i=i+1){
            for(int j=0;j<Arrayint[i].length;j=j+1){
                Arrraydouble[i][j]=(float)(Arrayint[i][j]);
            }
        }
        return Arrraydouble;  //turn int to double
    }
    private double[][] tovolume(double[][] Ar){
        double[][] Arrayvolume = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Arrayvolume.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                if(base[i][j]>fluidContact && top_horizon_offset[i][j]<fluidContact){
                    Arrayvolume[i][j] = cell*cell*(fluidContact-Ar[i][j]);
                }
                else{
                    Arrayvolume[i][j] = 0; //base < fluidContact
                }
            }
        }
        return Arrayvolume;
    }
    private double[][] topersent(double[][] Ar){
        double[][] Arraypersent = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                Arraypersent[i][j] = ((fluidContact-Ar[i][j])/200)*100;
                if(Arraypersent[i][j] > 100){
                    Arraypersent[i][j] = 0;    // persent 0-100
                }
                else if(Arraypersent[i][j] < 0){
                    Arraypersent[i][j] = 0;    // persent 0-100
                }
            }
        }
        return Arraypersent;
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
