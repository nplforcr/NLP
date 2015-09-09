#!/usr/bin/perl
#author: Dingcheng Li Jan. 20, 10

###############################################################################
##                                                                           ##
## This file is part of ModelBlocks. Copyright 2009, ModelBlocks developers. ##
##                                                                           ##
##    ModelBlocks is free software: you can redistribute it and/or modify    ##
##    it under the terms of the GNU General Public License as published by   ##
##    the Free Software Foundation, either version 3 of the License, or      ##
##    (at your option) any later version.                                    ##
##                                                                           ##
##    ModelBlocks is distributed in the hope that it will be useful,         ##
##    but WITHOUT ANY WARRANTY; without even the implied warranty of         ##
##    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          ##
##    GNU General Public License for more details.                           ##
##                                                                           ##
##    You should have received a copy of the GNU General Public License      ##
##    along with ModelBlocks.  If not, see <http://www.gnu.org/licenses/>.   ##
##                                                                           ##
###########################################################################

use warnings;
use strict;

my $dir  = "/home/dingcheng/Documents/corpora/muc/muc6/data/keys";
my $dir2 = "/home/dingcheng/Documents/NLPWorkspace/modelblocks/wsjparse/corefResult/462179mar21ES.cochain";




my %wordindex = ();

# it is an array to hold elements of entities.
my @coeva = ();

# it is an array of array to hold arrays which include coreference chains.
my @array_of_coref = ();
my @array_of_comentions=();
open( FILE,  "$dir" );
open( FILE2, ">$dir2" );
my $count = 0;
my @words = ();

while (<FILE>) {
	chomp;
	#$ht is the number, $hr is the features, $hw is the word, $hs is the same as $hr 
	if ( ( my $ht, my $hr, my $hw, my $hs ) =
		( $_ =~ /HYPOTH +([^ ]*) +([^ ]*) +([^ ]*) +(.*)/ ) )
	{
		my $T   = int $ht;
		#split main parts, op+CR and POS
		my @H   = split( /;/, $hr );
		#CR is split by ,
		my @CR  = split( /,/, $H[1] );
		my $word = $hw;
		#print "word word ************************: ", $word,"\n";
		#then index is split into individual part
		my @ind = split( /_/, $CR[0] );

		#print $hw, "\n";
		if ( $H[0] eq "NEW" ) {
			$count++;
			#@coeva is an array to hold count of mentions
			push( @coeva, $count );
			my @coarray = ();
			push( @coarray, $count );
			my @comention = ();
			push( @comention, $word );
			#put coarray into array of coref
			#in coarray, count is stored.
			#I see, after we get a new, we generate an array to hold it, then, store them into 
			#the array_of_coref even if there is only one element in each coarray
			push @array_of_coref, [@coarray];
			push @array_of_comentions, [@comention];
			print $count," ",$H[0]," ", $hw,"\n";
		}
		elsif ( $H[0] eq "old" ) {
			$count++;
			push( @coeva, $count );
			#$size is the length of an index string
			my $size = @ind;
			for ( my $i = 0 ; $i < $size ; $i++ ) {
				#when $i is smaller than $ind[$i], it implies that the replaced index is found
				if ( $i < $ind[$i] ) {
					# backtrack is equal the size minus the i, say 6-3=3
					my $backtrack   = $size - $i;
					#size of aoc is the size of array_of_coref
					my $size_of_aoc = @array_of_coref;
					#take out the array from array_of_coref and then add the old one which refers to 
					#the item into the array, so, the array will collect all coreferring item into one array
					my $array = $array_of_coref[ $size_of_aoc - $backtrack ];
					my $wordArray =$array_of_comentions[ $size_of_aoc - $backtrack ];

					#dereference is important!
					my @coarray = @{$array};
					my @cowordArray=@{$wordArray};
					# $count is the index of old element, 
					push( @coarray, $count );
					push(@cowordArray,$word);
					print $count," ", $coarray[0],"\n";

					#the following way doesn't work
					#push @coarray, [$count];
					#the following funciont does such a thing, replace the array in @array_of_coref with 
					#the new array @coarray which has more items inside since coreferring items added to it
					splice @array_of_coref, $size_of_aoc - $backtrack, 1, [@coarray];
					splice @array_of_comentions, $size_of_aoc - $backtrack, 1, [@cowordArray];
				   #splice (@array_of_coref,$size_of_aoc-$backtrack,1,@coarray);
					last;
				}
			}
			
			print $count," ",$H[0]," ", $hw,"\n";
			#print "count: ", $count, "\n";
		}
	}
}

my $size_coref_array = @array_of_coref;
print "size of coref_array: ",$size_coref_array, "\n";
############## the following works as well   #####################################
#    for my $i ( 0 .. $#array_of_coref ) {
#        for my $j ( 0 .. $#{$array_of_coref[$i]} ) {
#            print "elt $i $j is $array_of_coref[$i][$j]\n";
#        }
#    }
##################################################################################

# the following can get the same results as above one.
for my $i ( 0 .. $#array_of_coref ) {
	my @ithArray = @{ $array_of_coref[$i] };
	my @ithWordArray = @{ $array_of_comentions[$i] };
	my $sizeIthArray = @ithArray;
	if ( $sizeIthArray > 1 ) {
		for my $j ( 0 .. $#ithArray-1 ) {
			#print "elt $j is $ithArray[$j]\n";
			print "IDENT $ithArray[$j] $ithWordArray[$j] $ithArray[$j+1] $ithWordArray[$j+1]\n";
			print FILE2 "IDENT $ithArray[$j] $ithArray[$j+1]\n";
		}
	}
}

#print $size_coref_array, "\n";
#
#for(my $i=0;$i<$size_coref_array;$i++){
#	my @coarray = $array_of_coref[$i];
#	my $size_coarray=@coarray;
#	#print $size_coarray, "\n";
#	#if($size_coarray>1){
#		for(my $j=0;$j<$size_coarray;$j++){
#		print "IDENT ", $coarray[$j], $coarray[$j+1],"\n";
#		print FILE2 "IDENT ", $coarray[$j], "\n";
#	#}
#	}
#}

close(FILE);
close(FILE2);
