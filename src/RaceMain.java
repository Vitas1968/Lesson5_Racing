import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

public class RaceMain
{
    public static final int CARS_COUNT = 4;
    public static void main(String[] args) {

        //используем  Semaphore для доступа половины участников в тоннель
        Semaphore sem=new Semaphore(CARS_COUNT/2,true);
        // запускаем потоки через ExecutorService
        ExecutorService executorService= Executors.newFixedThreadPool(CARS_COUNT);
        // используем CyclicBarrier для синхронизации потоков на старте и финише
        CyclicBarrier start=new CyclicBarrier(CARS_COUNT+1);
        CyclicBarrier finish=new CyclicBarrier(CARS_COUNT+1);

        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++)
        {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10),sem,start,finish);
            executorService.execute( cars[i]);
        }
        executorService.shutdown();
        try
        {
            start.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (BrokenBarrierException e)
        {
            e.printStackTrace();
        }
//        for (int i = 0; i < cars.length; i++)
//        {
//            new Thread(cars[i]).start();
//        }
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
        try
        {
            finish.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (BrokenBarrierException e)
        {
            e.printStackTrace();
        }
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
    }
}
 class Car implements Runnable {
    private static int CARS_COUNT;
    static {
        CARS_COUNT = 0;
    }
    // добавляем ссылку на семафор в поток
    private Semaphore semaphore;
     private CyclicBarrier start;
     private CyclicBarrier finish;
    private Race race;
    private int speed;
    private String name;
    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed, Semaphore semaphore,CyclicBarrier start,CyclicBarrier finish) {
        this.race = race;
        this.speed = speed;
        this.semaphore=semaphore;
        this.start=start;
        this.finish=finish;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }
    @Override
    public void run() {
        try {

            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try
        {
            start.await();
            System.out.println(name + " стартанул!");
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (BrokenBarrierException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++)
        {
            if (race.getStages().get(i) instanceof Tunnel)
            { try
                {
                    System.out.println(name + " ожидает разрешение на въезд в туннель");
                    semaphore.acquire(); // запрос разрешения у семафора
                    race.getStages().get(i).go(this);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } finally
            {
                semaphore.release();
            }

            } else race.getStages().get(i).go(this);
        }

        try
        {
            System.out.println(name + " завершил дистанцию");
            finish.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (BrokenBarrierException e)
        {
            e.printStackTrace();
        }
    }
}
 abstract class Stage {
    protected int length;
    protected String description;
    public String getDescription() {
        return description;
    }
    public abstract void go(Car c);
}
 class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "Дорога " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " закончил этап: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
 class Tunnel extends Stage {
    public Tunnel() {
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            try {
               // System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
 class Race {
    private ArrayList<Stage> stages;
    public ArrayList<Stage> getStages() { return stages; }
    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}
