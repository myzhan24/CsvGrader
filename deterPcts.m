function [ ret ] = deterPcts( data )
%uses a nx4 matrix and converts each row to percents
%   takes in the matrix created by getPcts to create an nx4 matrix of
%   percents for the area charts.
    a = data(1,1);
    b = data(1,2);
    c = data(1,3);
    d = data(1,4);
    sum = (a+b+c+d);

    ret = [a/sum,b/sum,c/sum,d/sum];
    m = size(data,1);
    for i=2:m
        a = data(i,1);
        b = data(i,2);
        c = data(i,3);
        d = data(i,4);
        sum = (a+b+c+d);
        
        ret = [ret;[a/sum,b/sum,c/sum,d/sum]];
    end

end

