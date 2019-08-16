package by.students.grsu.entities.auction;

import by.students.grsu.entities.lot.Lot;
import by.students.grsu.entities.lot.LotStatus;
import by.students.grsu.entities.services.AuctionPlatformObserver;
import by.students.grsu.entities.services.LotFollower;
import by.students.grsu.entities.services.SoldLotFollower;
import by.students.grsu.entities.users.Follower;
import by.students.grsu.entities.users.User;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ActiveAuction extends Thread implements ActiveAuctionInterface{
    private Auction auction;
    private List<Follower> userFollowers;
    private AuctionFollower auctionFollower;
    private LotFollower lotFollower;
    private SoldLotFollower soldLotFollower;
    private AuctionPlatformObserver auctionPlatformObserver;
    public ActiveAuction(Auction auction){
        auction.makeActive();
        this.auction=auction;
        userFollowers= new ArrayList<Follower>();
        this.start();
    }

    @Override
    public void run(){
        //DEBUG
        System.out.println("Auction " + auction.getID() + " started");

        while(true){
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting observers");
            }
            //System.out.println("waiting...");
            if(auctionFollower==null)continue;
            if(lotFollower==null)continue;
            if(soldLotFollower==null)continue;
            if(auctionPlatformObserver==null)continue;
            break;
        }
        int tick = auction.getTick()*1000;
        LocalTime endTime = LocalTime.now().plusMinutes(auction.getMaxDuration());
        //System.out.println("Tick time = "+tick);
        //System.out.println("End time = "+endTime);
        List<Lot> lots;
        boolean auctionEnd;
        while(endTime.isAfter(LocalTime.now())){
            try {
                sleep(tick);
            } catch (InterruptedException e) {
                System.out.println("ActiveAuction " + auction.getID() + " interrupted");
            }
            auctionEnd=true;
            lots = auction.getLots();
            for(Lot lot : lots)
                if (lot.getStatus() == LotStatus.Registered){
                    //System.out.println(lot.getID() + ": " + lot.getPriceStep());
                    //lot.setCurrentPrice(lot.getCurrentPrice() - lot.getPriceStep());
                    lot.makePriceStep();
                  //  System.out.println(lot.getCurrentPrice());
                    auctionEnd=false;
                }
            if(auctionEnd)break;
            tickHappened();
        }
        auctionEnded();
    }

    @Override
    public List<Lot> getLots() {
        return auction.getLots();
    }


    @Override
    public void buyLot(int id, User user) throws Exception {
        for(Lot lot : auction.getLots())
            if(lot.getID()==id){lot.setSold();
                lotSold(id,user,lot.getCurrentPrice());
                break;
            }
    }

    public void joinAuctionFollower(AuctionFollower auctionFollower){
        this.auctionFollower=auctionFollower;
    }
    public void joinLotFollower(LotFollower lotFollower){
        this.lotFollower = lotFollower;
    }
    public void joinSoldLotFollower(SoldLotFollower soldLotFollower){
        this.soldLotFollower=soldLotFollower;
    }
    public void joinPlatformObserver(AuctionPlatformObserver platformObserver){
        auctionPlatformObserver=platformObserver;
    }
    @Override
    public void join(Follower follower) {
        userFollowers.add(follower);
    }
    public int getAuctionId(){
        return auction.getID();
    }
    @Override
    public void leave(Follower follower) {
        userFollowers.remove(follower);
    }

    private void tickHappened(){
        for(Follower follower : userFollowers)
            follower.tickHappened();
    }
    private void lotSold(int id,User user,double price){
        soldLotFollower.lotSold(user.getUsername(),id,price);
        lotFollower.lotSold(id);
        for(Follower follower : userFollowers)
            follower.lotSold();
    }
    private void auctionEnded(){
        System.out.println("Auction " + auction.getID() + " ended");
        auctionFollower.auctionEnded(auction.getID());
        lotFollower.auctionEnded(auction.getID());
        auctionPlatformObserver.auctionEnded(this);
        for(Follower follower : userFollowers)
            follower.auctionEnded();
        //TODO inform users
    }
}
