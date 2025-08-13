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
        setthis();          // set object
    }
    public void set(String str){  //set object (by method)
        this.txt = str;
        setthis();                // set object
    }
    public void setfluidcontact(double newinput) { //set object (by method)
        this.fluidContact = newinput;
        setthis();           //set fluidcontact and update all attribute
    }
    private void setthis() {
        base = toinfoBase(txt);                   //to base
        top_horizon_offset = toinfoTop(base);     //turn base into Top
        volume = tovolume(top_horizon_offset);
        persentage = topersent(top_horizon_offset);
    }
    private double[][] toinfoBase (String str) {
        String[] splitline = str.split("\n");  // count line ( .length ) by "\n"
        String str2 = str.replaceAll("\n", " ");  //turn "\n" into " "
        String[] infoStringAr = str2.split(" ");    // All info []

        int n=splitline.length;    // count line

        int col=n;
        int row=(infoStringAr.length)/n;
        System.out.println("col:"+col);  // test output
        System.out.println("row:"+row);  // test output
        double[][] infoBaseAr = new double[col][row];
        int count=0;            //count
        for(int i=0;i<col;i=i+1){
            for(int j=0;j<row;j=j+1){
                infoBaseAr[i][j]=Double.parseDouble(infoStringAr[count]);
                count=count+1;  //count
            }
        }
        return infoBaseAr;
    }
    private double[][] toinfoTop (double[][] Ar){    //Ar = base                   // to Top
        double[][] tophorizon = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){   // Ar = array base
                tophorizon[i][j]=Ar[i][j]-200;   // top = base-200
                if(tophorizon[i][j]<0){
                    tophorizon[i][j]=0;          //not -
                }
            }
        }
        return tophorizon;
    }
    private double[][] tovolume(double[][] Ar){   //Ar = Top
        double[][] Arrayvolume = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Arrayvolume.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                if(base[i][j]<fluidContact){         //Base higher than fluid
                    Arrayvolume[i][j]=cell*cell*200;
                }
                else if(base[i][j]>fluidContact && top_horizon_offset[i][j]<fluidContact){
                    Arrayvolume[i][j] = cell*cell*(fluidContact-Ar[i][j]);
                }
                else{   // Top under fluid
                    Arrayvolume[i][j] = 0;
                }
            }
        }
        return Arrayvolume;
    }
    private double[][] topersent(double[][] Ar){  //Ar = Top
        double[][] Arraypersent = new double[Ar.length][Ar[0].length];
        for(int i=0;i<Ar.length;i=i+1){
            for(int j=0;j<Ar[i].length;j=j+1){
                Arraypersent[i][j] = ((fluidContact-Ar[i][j])/200)*100; //Ar = TOP
                if(Arraypersent[i][j] > 100){    //Base higher than fluid
                    Arraypersent[i][j] = 100;    // persent 0-100
                }
                else if(Arraypersent[i][j] < 0){
                    Arraypersent[i][j] = 0;     // persent 0-100
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
    public String gettxt() {
        return this.txt;
    }
    public String toString() {
        return "savadee kub";
    }
}
