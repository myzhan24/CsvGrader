function [ pct ] = returnpctbelow( dist , num )
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here
total = 0;
n = size(dist,1);
for i=1:n

    if(dist(i) < num)
        total = total + 1;
    end
    
end
pct = total./n .* 100;
    
end


