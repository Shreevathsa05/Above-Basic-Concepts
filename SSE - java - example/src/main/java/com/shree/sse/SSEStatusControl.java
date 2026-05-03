package com.shree.sse;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/status")
public class SSEStatusControl {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping("/subscribe/{jobId}")
    public SseEmitter subscribe(@PathVariable String jobId){
        System.out.println("Subscribing to "+ jobId);
        var emit = new SseEmitter(0L);

        emitters.put(jobId,emit);

        emit.onCompletion(
                ()->{
                    System.out.println("Connection Closed "+jobId);
                    emitters.remove(jobId);
                    System.out.println("Size of Emitter - "+emitters.size());
                }
        );
        return emit;
    }

    @PostMapping("/start/{jobId}")
    public Map<String,String> startJob(@PathVariable String jobId){
        new Thread(() -> processJob(jobId)).start();
        return Map.of("message", "Job Started"+jobId);
    }

//    Per Job processor
    public void processJob(String jobId){
        try {
            sendUpdate(jobId, "PENDING....");
            Thread.sleep(2000);
            sendUpdate(jobId, "UPLOADING....");
            Thread.sleep(2000);
            sendUpdate(jobId,"PROCESSING 25%....");
            Thread.sleep(2000);
            sendUpdate(jobId,"PROCESSING 50%....");
            Thread.sleep(2000);
            sendUpdate(jobId,"PROCESSING 75%...");
            Thread.sleep(2000);
            sendUpdate(jobId,"PROCESSING 100%...");
            Thread.sleep(2000);
            sendUpdate(jobId, "COMPLETED");
            emitters.get(jobId).complete();
        } catch (Exception e) {
            e.printStackTrace();
            sendUpdate(jobId,"FAILED");
        }
    }

//    Updates sender
    public void sendUpdate(String jobId, String status){
        SseEmitter serverEvent=emitters.get(jobId);
        System.out.println(serverEvent);

        if(serverEvent!=null){
            try{
                serverEvent.send(status);
            }catch(Exception ex){
                serverEvent.complete();
                emitters.remove(jobId);
                ex.printStackTrace();
            }
        }
    }
}
