function [ pcntData ] = getPcts( time, data )
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

   time2x = sortrows([time,data]);
   initval = time2x(1,2);
   pcntData = [1,0,0,0];
   
   if(initval == 1)
       pcntData = [1,0,0,0];
   elseif(initval == 2)
       pcntData = [0,1,0,0];
   elseif(initval==3)
       pcntData = [0,0,1,0];
   elseif(initval == 4)
       pcntData = [0,0,0,1];
   end

   m = size(data);
   for i=1:m-1
       initval = time2x(i+1,2);
       if(initval == 1)
          pcntData = [pcntData; [pcntData(i,1)+1, pcntData(i,2), pcntData(i,3), pcntData(i,4) ]]; 
       elseif(initval == 2)
          pcntData = [pcntData; [pcntData(i,1), pcntData(i,2)+1, pcntData(i,3), pcntData(i,4) ]]; 
       elseif(initval==3)
          pcntData = [pcntData; [pcntData(i,1), pcntData(i,2), pcntData(i,3)+1, pcntData(i,4) ]]; 
       elseif(initval == 4)
          pcntData = [pcntData; [pcntData(i,1), pcntData(i,2), pcntData(i,3), pcntData(i,4)+1 ]]; 
       end 
   end
 
end

