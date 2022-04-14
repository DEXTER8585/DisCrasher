package com.authxero.discrash.helpers;

import com.authxero.discrash.executor.CommandExecutorService;
import com.authxero.discrash.executor.FireAndForgetExecutor;
import com.authxero.discrash.storage.FileSystemStorageService;
import java.util.concurrent.TimeUnit;

public class FFMPEGHelper {
    private static CommandExecutorService ces = null;
    private static FireAndForgetExecutor ffe = null;
    public static FFMPEGHelper instance =  null;

    public FFMPEGHelper() {
        ces = new CommandExecutorService();
        ffe = new FireAndForgetExecutor();
        instance = this;
    }

    public String generateCrashVideo(String fileNameInput) throws Exception {
        try{
            System.out.println("Beginning to generate file "+ fileNameInput);
            String tempFileName = UtilHelper.generateFileName(32);
            String tempFileName2 = UtilHelper.generateFileName(32);
            Process proc = ces.fireAndReturnProcess("ffmpeg -i "+ FileSystemStorageService.rootLocation.resolve(fileNameInput)+" -pix_fmt yuv444p "+FileSystemStorageService.outputLocation.resolve(tempFileName+".webm").toAbsolutePath());
            proc.waitFor(40, TimeUnit.SECONDS);
            UtilHelper.createFileAndWrite(tempFileName+".txt","file "+tempFileName+".webm\nfile black.webm");
            Process proc2 = ces.fireAndReturnProcess("ffmpeg -f concat -i "+FileSystemStorageService.outputLocation.resolve(tempFileName+".txt").toAbsolutePath().toString()+" -codec copy "+FileSystemStorageService.outputLocation.resolve(tempFileName2+".webm"));
            proc2.waitFor(30, TimeUnit.SECONDS);
            UtilHelper.deleteFile(tempFileName+".txt");
            UtilHelper.deleteFile(FileSystemStorageService.rootLocation.resolve(fileNameInput).toAbsolutePath().toString());
            UtilHelper.deleteFile(tempFileName+".webm");
            FireAndForgetRunnable forgetRunnableHelper = new FireAndForgetRunnable(() -> {
                TimeUnit.MINUTES.sleep(10);
                UtilHelper.deleteFile(tempFileName2+".webm");
            });
            ffe.exec(forgetRunnableHelper);
            System.out.println(tempFileName2+" generated! File will be deleted in 10 minutes.");
            return tempFileName2+".webm";
        }
        catch (Exception ex){
            throw new Exception(ex.getMessage(),ex);
        }
    }

}
