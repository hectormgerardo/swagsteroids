package swagsteroids;

import java.awt.*;
import java.applet.Applet;

class Mineral {
 static int width, height;
 Polygon shape;
 boolean active;
 double  angle,deltaAngle,currentX, currentY,deltaX, deltaY;
 Polygon figureichon;
 public Mineral(){

 this.shape = new Polygon();
 this.active = false;
 this.angle = 0.0;
 this.deltaAngle = 0.0;
 this.currentX = 0.0;
 this.currentY = 0.0;
 this.deltaX = 0.0;
 this.deltaY = 0.0;
 this.figureichon = new Polygon();
}
 public void avanzar(){
 this.angle += this.deltaAngle;
 if (this.angle < 0)
 this.angle += 2 * Math.PI;
 if (this.angle > 2 * Math.PI)
 this.angle -= 2 * Math.PI;
 this.currentX += this.deltaX;
 if (this.currentX <-width / 2)
 this.currentX += width;
 if (this.currentX > width / 2)
 this.currentX -= width;
 this.currentY -= this.deltaY;
 if (this.currentY <-height / 2)
 this.currentY += height;
 if (this.currentY > height / 2)
 this.currentY -= height;
}
 public void render(){
 this.figureichon = new Polygon();
 for(int i = 0; i < this.shape.npoints; i++) this.figureichon.addPoint((int) Math.round(this.shape.xpoints[i]* Math.cos(this.angle)+ this.shape.ypoints[i]* Math.sin(this.angle))+(int) Math.round(this.currentX)+width /2,
(int)Math.round(this.shape.ypoints[i]* Math.cos(this.angle)- this.shape.xpoints[i]* Math.sin(this.angle))+(int) Math.round(this.currentY)+ height / 2);
}
 public boolean isCrashin(Mineral s){
 for(int i=0;i<s.figureichon.npoints;i++)
     if (this.figureichon.inside(s.figureichon.xpoints[i], s.figureichon.ypoints[i])) return true;
 for (int i=0;i<this.figureichon.npoints;i++) if (s.figureichon.inside(this.figureichon.xpoints[i], this.figureichon.ypoints[i])) return true;
 return false;
}
}
public class Swagsteroids extends Applet implements Runnable {
 Thread loadThread;
 Thread loopThread;
 static final int lag=50,lives=3,shotLimit=6,mineralLimit=8;
 static final int maxDestroyd = 20,numDestroyd = 30,numInvisible = 60;
 static final int Peace = 30;
//minimos y maximos pa los astoroides 
 static final int sidesMin=8,sidesMax=12,sizeMin=20,sizeMax=40,speedMin=2,speedMax=12;
 //puntajes
 static final int booty1 = 25,booty2 = 50;
 static final int projectileBooty = 500;
 static final int OneUp = 5000;
 int  numStars;
 Point[] stars;
 int score1, score2;
 int best;
 boolean loaded = false;
 boolean paused;
 boolean playing;
 boolean detail;
 boolean left=false,right=false,up=false,down=false;
 Mineral  pirateShip;
 Mineral  projectile;
 Mineral[] bolitas = new Mineral[shotLimit];
 Mineral[] mineralz = new Mineral[mineralLimit];
 Mineral[] explosions = new Mineral[maxDestroyd];
int livesLeft;
 int shipCounter;
 int invisibleCounter;
 int[] bolitaCounter=new int[shotLimit];
 int  bolitaIndex,projectileCounter;
 boolean[] asteroidIsSmall = new boolean[mineralLimit];
 int  mineralzCounter,mineralzSpeed,mineralzLeft,explosionIndex;
 int[] explosionCounter = new int[maxDestroyd];
 boolean oarsPlaying;
 boolean projectilePlaying;
 Dimension ZDimension;
 Image ZImage;
 Graphics ZGFX;
 Font font = new Font("Arial", Font.PLAIN, 12);
 FontMetrics fm;
 int fontWidth,fontHeight;
 public void init(){
 Graphics g;
 Dimension d;
 this.setSize(new Dimension(640,480));
 g = getGraphics();d = getSize();
 Mineral.width = d.width;
 Mineral.height = d.height;
 numStars = Mineral.width * Mineral.height / 5000;
 stars = new Point[numStars];
 for (int i = 0; i < numStars; i++)
 stars[i]= new Point((int)(Math.random()* Mineral.width),(int)(Math.random()* Mineral.height));
 pirateShip = new Mineral();
 pirateShip.shape.addPoint(0,-10);
 pirateShip.shape.addPoint(7, 10);
 pirateShip.shape.addPoint(-7, 10);
 for (int i = 0; i < shotLimit; i++){
 bolitas[i]= new Mineral();
 bolitas[i].shape.addPoint(1, 1);
 bolitas[i].shape.addPoint(1,-1);
 bolitas[i].shape.addPoint(-1, 1);
 bolitas[i].shape.addPoint(-1,-1);
}
 projectile = new Mineral();
 projectile.shape.addPoint(0,-4);
 projectile.shape.addPoint(1,-3);
 projectile.shape.addPoint(1, 3);
 projectile.shape.addPoint(2, 4);
 projectile.shape.addPoint(-2, 4);
 projectile.shape.addPoint(-1, 3);
 projectile.shape.addPoint(-1,-3);
 for (int i = 0; i < mineralLimit; i++)
 mineralz[i]= new Mineral();
 for (int i = 0; i < maxDestroyd; i++)
 explosions[i]= new Mineral();
 g.setFont(font);
 fm = g.getFontMetrics();
 fontWidth = fm.getMaxAdvance();
 fontHeight = fm.getHeight();
 best = 0;
 detail = true;
 introGame();
 outroGame();
}
 public void introGame(){
 score1=0; livesLeft=lives;mineralzSpeed=speedMin; score2=OneUp;
 initShip();
 initBolitas();
 stopProjectile();
 initMinerals();
 initKeelhauls();
 playing = true;
 paused = false;
     System.out.println("game ready");
}
 public void outroGame(){
 playing = false;
 stopShip();
 stopProjectile();
     System.out.println("game over");
}
 public void start(){
 if (loopThread == null){
 loopThread = new Thread(this);
 loopThread.start();
}
 if (!loaded && loadThread == null){
 loadThread = new Thread(this);
 loadThread.start();
}
}
 public void stop(){
 if (loopThread != null){
     loopThread.stop();
     loopThread = null;}
 if (loadThread != null){
     loadThread.stop();
     loadThread = null;}
}
 public void run(){
 int i, j;
 long startTime;
 Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 startTime = System.currentTimeMillis();
 if (!loaded && Thread.currentThread()== loadThread){
 loaded = true;
 loadThread.stop();
}
 while (Thread.currentThread()== loopThread){
 if (!paused){
 updateShip();
 updateBolitas();
 updateProjectile();
 updateMinerals();
 updateKeelhauls();
 if (score1 > best)
 best = score1;
 if (score1 > score2){
 score2 += OneUp;
 livesLeft++;
}
// If all mineralz have been destroyd, crate moar.
 if (mineralzLeft <= 0)
     if (--mineralzCounter <= 0) initMinerals();
 }
//EL MENTADO REPAINT(); >.<!!!
 repaint();
 try {
     startTime += lag;
     Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
 }
 catch (InterruptedException e){break;}
 }}
 public void initShip(){
 pirateShip.active = true;
 pirateShip.angle = 0.0;
 pirateShip.deltaAngle = 0.0;
 pirateShip.currentX = 0.0;
 pirateShip.currentY = 0.0;
 pirateShip.deltaX = 0.0;
 pirateShip.deltaY = 0.0;
 pirateShip.render();
 oarsPlaying = false;
 invisibleCounter = 0;
}
 public void updateShip(){
 double dx, dy, limit;
 if (!playing)
     return;
 if (left){
     pirateShip.angle += Math.PI / 16.0;
     if (pirateShip.angle > 2 * Math.PI)
         pirateShip.angle -= 2 * Math.PI;
 }
 if (right){
     pirateShip.angle -= Math.PI / 16.0;
     if (pirateShip.angle < 0)pirateShip.angle += 2 * Math.PI;
 }
 dx =-Math.sin(pirateShip.angle);
 dy = Math.cos(pirateShip.angle);
 limit = 0.8 * sizeMin;
 if (up){
     if (pirateShip.deltaX + dx >-limit && pirateShip.deltaX + dx < limit) pirateShip.deltaX += dx;
     if(pirateShip.deltaY + dy >-limit && pirateShip.deltaY + dy < limit) pirateShip.deltaY += dy;
 }
 if (down){
     if (pirateShip.deltaX - dx >-limit && pirateShip.deltaX - dx < limit) pirateShip.deltaX -= dx;
     if(pirateShip.deltaY - dy >-limit && pirateShip.deltaY - dy < limit) pirateShip.deltaY -= dy;
 }
 if (pirateShip.active){
     pirateShip.avanzar();
     pirateShip.render();
     if (invisibleCounter > 0)invisibleCounter--;
 }
 else
     if (--shipCounter <= 0)
         if (livesLeft > 0){
             initShip();
             invisibleCounter = numInvisible;
         }
 else outroGame();
}
 public void stopShip(){
 pirateShip.active=false;
 shipCounter=numDestroyd;
 if (livesLeft > 0) livesLeft--;
 if (loaded) oarsPlaying=false;
}
 public void initBolitas(){
 for(int i = 0; i < shotLimit; i++){
     bolitas[i].active = false;
     bolitaCounter[i]= 0;}
 bolitaIndex=0;}
 public void updateBolitas(){
 for(int i=0; i < shotLimit; i++)
     if (bolitas[i].active){
         bolitas[i].avanzar();
         bolitas[i].render();
         if (--bolitaCounter[i]< 0)
             bolitas[i].active = false;
     }}
 public void initProjectile(){
     projectile.active = true;
     projectile.angle = 0.0;
     projectile.deltaAngle = 0.0;
     projectile.deltaX = 0.0;
     projectile.deltaY = 0.0;
     projectile.render();
     projectileCounter = 3 * Math.max(Mineral.width, Mineral.height)/ sizeMin;
     projectilePlaying = true;
 }
 public void updateProjectile(){
 if (projectile.active){
     if (--projectileCounter <= 0) stopProjectile();
 else {
         guideProjectile();
         projectile.avanzar();
         projectile.render();
         for (int i=0; i<shotLimit;i++)
             if (bolitas[i].active && projectile.isCrashin(bolitas[i])){
                 keelhaul(projectile);
                 stopProjectile();
                 score1+=projectileBooty;
             }
         if (projectile.active&&pirateShip.active&&invisibleCounter<=0&&pirateShip.isCrashin(projectile)){
             keelhaul(pirateShip);
             stopShip();
             stopProjectile();
         }
     }
 }
 }
 public void guideProjectile(){
     double dx, dy, angle;
     if (!pirateShip.active || invisibleCounter > 0) return;
     dx = pirateShip.currentX - projectile.currentX;
     dy = pirateShip.currentY - projectile.currentY;
     if (dx==0 && dy == 0) angle=0;
     if (dx==0){
         if (dy<0) angle =-Math.PI/2;
         else angle = Math.PI/2;}
     else {angle=Math.atan(Math.abs(dy/dx));
     if (dy>0) angle =-angle;
     if (dx < 0) angle = Math.PI-angle;}
     projectile.angle = angle-Math.PI/2;
     projectile.deltaX = sizeMin/3*-Math.sin(projectile.angle);
     projectile.deltaY = sizeMin/3*Math.cos(projectile.angle);
}
 public void stopProjectile(){
     projectile.active=false;
     projectileCounter=0;
     if (loaded)projectilePlaying=false;
 }
 public void initMinerals(){
 int x,y,s;
 double tit, r;
 for (int i = 0; i < mineralLimit; i++){
     mineralz[i].shape = new Polygon();
     s=sidesMin+(int)(Math.random()*(sidesMax-sidesMin));
     for (int j=0; j<s;j++){
         tit=2 * Math.PI / s * j;
         r=sizeMin+(int)(Math.random()*(sizeMax-sizeMin));
         x=(int)-Math.round(r * Math.sin(tit));
         y=(int) Math.round(r * Math.cos(tit));
         mineralz[i].shape.addPoint(x, y);
}
     mineralz[i].active = true;
     mineralz[i].angle = 0.0;
     mineralz[i].deltaAngle =(Math.random()- 0.5)/ 10;
     if (Math.random()< 0.5){
         mineralz[i].currentX =-Mineral.width/2;
         if (Math.random()< 0.5) mineralz[i].currentX=Mineral.width/2;
         mineralz[i].currentY = Math.random()* Mineral.height;
}
     else {
         mineralz[i].currentX=Math.random()* Mineral.width;
         mineralz[i].currentY=-Mineral.height/2;
         if (Math.random()< 0.5)
             mineralz[i].currentY=Mineral.height/2;
     }
     mineralz[i].deltaX = Math.random()* mineralzSpeed;
     if (Math.random()< 0.5)
         mineralz[i].deltaX =-mineralz[i].deltaX;
     mineralz[i].deltaY = Math.random()* mineralzSpeed;
     if (Math.random()< 0.5)
         mineralz[i].deltaY =-mineralz[i].deltaY;
     mineralz[i].render();
     asteroidIsSmall[i]= false;
 }
 mineralzCounter=Peace;
 mineralzLeft=mineralLimit;
 if (mineralzSpeed<speedMax)mineralzSpeed++;
}
 public void initSmallMinerals(int n){
 int count,x,y,i,s;
 double tempX, tempY,tit, r;
 count = 0;
 i = 0;
 tempX=mineralz[n].currentX; tempY = mineralz[n].currentY;
 do{
 if (!mineralz[i].active){
 mineralz[i].shape = new Polygon();
 s = sidesMin +(int)(Math.random()*(sidesMax- sidesMin));
 for (int j = 0; j < s; j ++){
     tit = 2 * Math.PI / s * j;
     r =(sizeMin +(int)(Math.random()*(sizeMax - sizeMin)))/ 2;
     x =(int)-Math.round(r * Math.sin(tit));
     y =(int) Math.round(r * Math.cos(tit));
     mineralz[i].shape.addPoint(x, y);
 }
 mineralz[i].active = true;
 mineralz[i].angle = 0.0;
 mineralz[i].deltaAngle =(Math.random()- 0.5)/ 10;
 mineralz[i].currentX = tempX;
 mineralz[i].currentY = tempY;
 mineralz[i].deltaX = Math.random()* 2 * mineralzSpeed - mineralzSpeed;
 mineralz[i].deltaY = Math.random()* 2 * mineralzSpeed - mineralzSpeed;
 mineralz[i].render();
 asteroidIsSmall[i]= true;
 count++;
 mineralzLeft++;
} i++;}
 while (i<mineralLimit &&count<2);}
 public void updateMinerals(){
 for (int i=0;i<mineralLimit;i++)
 if (mineralz[i].active){
     mineralz[i].avanzar();
    mineralz[i].render();
    for (int j=0;j<shotLimit;j++)
        if (bolitas[j].active && mineralz[i].active && mineralz[i].isCrashin(bolitas[j])){
            mineralzLeft--;
            mineralz[i].active = false;
            bolitas[j].active = false;
            keelhaul(mineralz[i]);
            if (!asteroidIsSmall[i]){
                score1 += booty1;
                initSmallMinerals(i);}
 else score1 += booty2;
}
    if (pirateShip.active && invisibleCounter <= 0 && mineralz[i].active && mineralz[i].isCrashin(pirateShip)){
        keelhaul(pirateShip);
        stopShip();
        stopProjectile();
    }}}
 public void initKeelhauls(){
 for(int i=0; i<maxDestroyd; i++){
     explosions[i].shape = new Polygon();
     explosions[i].active = false;
     explosionCounter[i]= 0;
 }
 explosionIndex = 0;}
 public void keelhaul(Mineral s){
 int c,j;
 s.render();
 c=2;
 if (detail||s.figureichon.npoints<6) c=1;
 for (int i = 0; i < s.figureichon.npoints; i += c){
     explosionIndex++;
     if (explosionIndex >= maxDestroyd)explosionIndex = 0;
     explosions[explosionIndex].active = true; explosions[explosionIndex].shape=new Polygon();
     explosions[explosionIndex].shape.addPoint(s.shape.xpoints[i], s.shape.ypoints[i]);
     j=i+1;
     if (j >=s.figureichon.npoints) j-= s.figureichon.npoints;
     explosions[explosionIndex].shape.addPoint(s.shape.xpoints[j], s.shape.ypoints[j]);
     explosions[explosionIndex].angle = s.angle;
     explosions[explosionIndex].deltaAngle =(Math.random()* 2 * Math.PI - Math.PI)/ 15;
     explosions[explosionIndex].currentX = s.currentX;
     explosions[explosionIndex].currentY = s.currentY;
     explosions[explosionIndex].deltaX =-s.shape.xpoints[i]/ 5;
     explosions[explosionIndex].deltaY =-s.shape.ypoints[i]/ 5;
     explosionCounter[explosionIndex]= numDestroyd;
 }}
 public void updateKeelhauls(){
 for (int i=0;i<maxDestroyd;i++)
     if (explosions[i].active){
         explosions[i].avanzar();
         explosions[i].render();
         if (--explosionCounter[i]<0)explosions[i].active=false;
     }}
 public boolean keyDown(Event e, int key){
 if (key == Event.LEFT) left=true;
 if (key == Event.RIGHT) right=true;
 if (key == Event.UP) up=true;
 if (key == Event.DOWN) down=true;
 if ((up||down)&&pirateShip.active&&!oarsPlaying){
     if (!paused)
         oarsPlaying=true;}
 if (key == 32 && pirateShip.active){
     bolitaIndex++;
     if (bolitaIndex >= shotLimit) bolitaIndex = 0;
     bolitas[bolitaIndex].active = true;
     bolitas[bolitaIndex].currentX = pirateShip.currentX;
     bolitas[bolitaIndex].currentY = pirateShip.currentY;
     bolitas[bolitaIndex].deltaX = sizeMin *-Math.sin(pirateShip.angle);
     bolitas[bolitaIndex].deltaY = sizeMin * Math.cos(pirateShip.angle);
     bolitaCounter[bolitaIndex]= Math.min(Mineral.width, Mineral.height)/ sizeMin;
 }
 if(key==104&&pirateShip.active&&invisibleCounter<=0){
     pirateShip.currentX = Math.random()* Mineral.width;
     pirateShip.currentX = Math.random()* Mineral.height;
     invisibleCounter=numInvisible;
 }
 //P key 4 pause
 if (key == 112){paused =!paused;}
 //D key 4 stars
 if (key == 100) detail =!detail;
//S key for starrrrt da geam
 if (key == 115 && loaded &&!playing)introGame();
 return true;
}
public boolean keyUp(Event e, int key){
    if (key == Event.LEFT) left=false;
    if (key == Event.RIGHT) right=false;
    if (key == Event.UP) up=false;
    if (key == Event.DOWN) down=false;
    if (!up &&!down && oarsPlaying)oarsPlaying = false;
    return true;
}
 public void paint(Graphics g){update(g);}
 public void update(Graphics g){
     Dimension d=this.getSize();
     int c;
     String s;
     if(ZGFX==null || d.width != ZDimension.width || d.height != ZDimension.height){
         ZDimension=d;
         ZImage=createImage(d.width, d.height);
         ZGFX=ZImage.getGraphics();
     }
     ZGFX.setColor(Color.black);
     ZGFX.fillRect(0, 0, d.width, d.height);
     if (detail){
         ZGFX.setColor(Color.white);
         for(int i = 0; i < numStars; i++)ZGFX.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
     }
ZGFX.setColor(Color.white);
for(int i = 0; i < shotLimit; i++)
    if (bolitas[i].active) ZGFX.drawPolygon(bolitas[i].figureichon);
c=Math.min(projectileCounter * 24, 255);
ZGFX.setColor(new Color(c, c, c));
if (projectile.active){
    ZGFX.drawPolygon(projectile.figureichon);
    ZGFX.drawLine(projectile.figureichon.xpoints[projectile.figureichon.npoints-1], projectile.figureichon.ypoints[projectile.figureichon.npoints-1],projectile.figureichon.xpoints[0], projectile.figureichon.ypoints[0]);
}
 for (int i = 0; i < mineralLimit; i++)
 if (mineralz[i].active){
     if (detail){
         ZGFX.setColor(Color.black);
         ZGFX.fillPolygon(mineralz[i].figureichon);}
     ZGFX.setColor(Color.white);
     ZGFX.drawPolygon(mineralz[i].figureichon);
     ZGFX.drawLine(mineralz[i].figureichon.xpoints[mineralz[i].figureichon.npoints-1], mineralz[i].figureichon.ypoints[mineralz[i].figureichon.npoints-1],mineralz[i].figureichon.xpoints[0], mineralz[i].figureichon.ypoints[0]);
}
c=255-(255/numInvisible)* invisibleCounter;
if (pirateShip.active){
    if (detail && invisibleCounter == 0){
        ZGFX.setColor(Color.black);
        ZGFX.fillPolygon(pirateShip.figureichon);
    }
    ZGFX.setColor(new Color(c, c, c));
    ZGFX.drawPolygon(pirateShip.figureichon);
    ZGFX.drawLine(pirateShip.figureichon.xpoints[pirateShip.figureichon.npoints - 1], pirateShip.figureichon.ypoints[pirateShip.figureichon.npoints - 1],pirateShip.figureichon.xpoints[0], pirateShip.figureichon.ypoints[0]);
}
 ZGFX.drawString(""+score1, fontWidth, fontHeight);
 ZGFX.drawString("Vidas:"+ livesLeft, fontWidth, d.height - fontHeight);
 s ="Best:"+ best;
 ZGFX.drawString(s, d.width -(fontWidth + fm.stringWidth(s)), fontHeight);
 if (!playing){
     s ="Hank, they're rocks."; ZGFX.drawString(s,(d.width - fm.stringWidth(s))/ 2, d.height / 2);
     s ="JESUS CHRIST MARIE, THEY'RE MINERALZ!"; ZGFX.drawString(s,(d.width - fm.stringWidth(s))/ 2, d.height / 2 + fontHeight);
     if (!loaded){s =""; ZGFX.drawString(s,(d.width - fm.stringWidth(s))/ 2, d.height / 4);}
     else {
         s="GAME OVER";
         ZGFX.drawString(s,(d.width - fm.stringWidth(s))/ 2, d.height / 4);
         s="press S to start playin'";
         ZGFX.drawString(s,(d.width - fm.stringWidth(s))/ 2, d.height / 4 + fontHeight);
     }}
 else if (paused){
     s ="Game Paused";
     ZGFX.drawString(s,(d.width - fm.stringWidth(s))/ 2, d.height / 4);}
 g.drawImage(ZImage, 0, 0, this);
}
}
