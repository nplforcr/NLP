#!/usr/bin/perl -w
use strict;
#################################
# History:
#
# version 03
#    * modified scoring formulas for relations and events that simplifies
#      scoring (reduces the number of scoring parameters) and eliminates
#      the value discontinuity between false alarms and partial matches
#
# version 02
#    * bug fixes in score computation
#    * improved diagnostic output for relations
#
# version 01
#    This utility provides evaluation functionality for the ACE program.
#    * Derives from ace04-eval-v12i.
#    * speed-up from Radu Florian integrated into code
#      - candidate_element_pairs speeds up computation of element mapping values
#      - get_cohorts speeds up mapping
#    * Updated to dtd version 5.0.2
#    * elements evaluated:  entities, relations, events, timex2s, and quantities
#    * evaluation added of reconciliation of elements with respect to external
#      databases - aka, the "END" task - Entity Normalization and Disambiguation
#
#################################

my %entity_attributes =
  (ID => {},
   CLASS => {GEN => 1, NEG => 1, SPC => 1, USP => 1},
   SUBTYPE => {Address => 1, Air => 1, Airport => 1, Biological => 1, Blunt => 1, Boundary => 1,
	       "Building-Grounds" => 1, Celestial => 1, Chemical => 1, Chemical => 1, Commercial => 1,
	       Continent => 1, "County-or-District" => 1, Educational => 1, Entertainment => 1, Exploding => 1,
	       "GPE-Cluster" => 1, Government => 1, "Illegal-Drug" => 1, International => 1, Land => 1,
	       "Land-Region-Natural" => 1, Media => 1, "Medical-Science" => 1, Military => 1, Nation => 1,
	       "Non-Governmental" => 1, Nuclear => 1, Nuclear => 1, Path => 1, Plant => 1, Political => 1,
	       "Population-Center" => 1, Professional => 1, Projectile => 1, "Region-General" => 1,
	       "Region-International" => 1, Religious => 1, Sharp => 1, Shooting => 1, Special => 1,
	       Sports => 1, "State-or-Province" => 1, "Subarea-Facility" => 1, "Subarea-Vehicle" => 1,
	       Underspecified => 1, Water => 1, "Water-Body" => 1},
   TYPE => {FAC => {Airport => 1, "Building-Grounds" => 1, Path => 1, Plant => 1, "Subarea-Facility" => 1},
	    GPE => {Continent => 1, "County-or-District" => 1, "GPE-Cluster" => 1, Nation => 1,
		    "Population-Center" => 1, Special => 1, "State-or-Province" => 1},
	    LOC => {Address => 1, Boundary => 1, Celestial => 1, "Land-Region-Natural" => 1,
		    "Region-General" => 1, "Region-International" => 1, "Water-Body" => 1},
	    ORG => {Commercial => 1, Educational => 1, Entertainment => 1, Government => 1, International => 1,
		    Media => 1, "Medical-Science" => 1, Military => 1, "Non-Governmental" => 1, Political => 1,
		    Professional => 1, Religious => 1, Sports => 1},
	    PER => {},
	    "SUB" => {Chemical => 1, "Illegal-Drug" => 1, Nuclear => 1},
	    VEH => {Air => 1, Land => 1, "Subarea-Vehicle" => 1, Underspecified => 1, Water => 1},
	    WEA => {Biological => 1, Blunt => 1, Chemical => 1, Exploding => 1, Nuclear => 1, Projectile => 1,
		    Sharp => 1, Shooting => 1, Underspecified => 1}});
my @entity_attributes = sort keys %entity_attributes;

my %quantity_attributes =
  (ID => {},
   SUBTYPE => {"E-Mail" => 1, Money => 1, Percent => 1, "Phone-Number" => 1, URL => 1},
   TYPE => {"Contact-Info" => {"E-Mail" => 1, "Phone-Number" => 1, URL => 1},
	    Crime => {},
	    Illness => {},
	    "Job-Title" => {},
	    Numeric => {Money => 1, Percent => 1},
	    Sentence => {},
	    "TIMEX2" => {}});
my @quantity_attributes = sort keys %quantity_attributes;

my %timex2_attributes =
  (ID => {},
   TIMEX2_ANCHOR_DIR => {AFTER => 1, AS_OF => 1, BEFORE => 1, ENDING => 1, STARTING => 1, WITHIN => 1},
   TIMEX2_ANCHOR_VAL => {},
   TIMEX2_COMMENT => {},
   TIMEX2_MOD => {AFTER => 1, APPROX => 1, BEFORE => 1, END => 1, EQUAL_OR_LESS => 1, EQUAL_OR_MORE => 1,
		  LESS_THAN => 1, MID => 1, MORE_THAN => 1, ON_OR_AFTER => 1, ON_OR_BEFORE => 1, START => 1},
   TIMEX2_NON_SPECIFIC => {YES => 1},
   TIMEX2_SET => {YES => 1},
   TIMEX2_VAL => {},
   TYPE => {"TIMEX2" => {}});
my @timex2_attributes = sort keys %timex2_attributes;

my %relation_attributes =
  (ID => {},
   SUBTYPE => {Artifact => 1, Business => 1, "Citizen-Resident-Ethnicity" => 1, Customer => 1, Employment => 1,
	       Family => 1, Founder => 1, Geographical => 1, Ideology => 1, "Investor-Shareholder" => 1,
	       "Lasting-Personal" => 1, Located => 1, Membership => 1, Near => 1, "Org-Location" => 1, Ownership => 1,
	       "Sports-Affiliation" => 1, "Student-Alum" => 1, Subsidiary => 1, "User-Owner-Inventor-Manufacturer" => 1},
   TYPE => {ART => {"User-Owner-Inventor-Manufacturer" => 1},
	    "GPE-AFF" => {"Citizen-Resident-Ethnicity" => 1, Ideology => 1, "Org-Location" => 1},
	    "ORG-AFF" => {Customer => 1, Employment => 1, Founder => 1, "Investor-Shareholder" => 1,
			  Membership => 1, Ownership => 1, "Sports-Affiliation" => 1, "Student-Alum" => 1},
	    "PART-WHOLE" => {Artifact => 1, Geographical => 1, Subsidiary => 1},
	    "PER-SOC" => {Business => 1, Family => 1, "Lasting-Personal" => 1},
	    PHYS => {Located => 1, Near => 1}});
my @relation_attributes = sort keys %relation_attributes;

my %relation_argument_roles =
  ("Arg-1" => 1, "Arg-2" => 1, "Time-After" => 1, "Time-As-Of" => 1, "Time-At-Beginning" => 1, "Time-At-End" => 1,
   "Time-Before" => 1, "Time-Ending" => 1, "Time-Holds" => 1, "Time-Starting" => 1, "Time-Within" => 1);
my @relation_argument_roles = sort keys %relation_argument_roles;

my %relation_symmetry =
  (PHYS     => {Near => 1},
   "PER-SOC" => {Business => 1, Family => 1, Other => 1});

my %event_attributes =
  (ID => {},
   GENERICITY => {Generic => 1, Specific => 1},
   MODALITY => {Asserted => 1, Believed => 1, Hypothetical => 1, Unspecified => 1},
   POLARITY => {Negative => 1, Positive => 1},
   SUBTYPE => {Acquit => 1, Arrest => 1, Attack => 1, "Be-Born" => 1, Charge => 1, Communicate => 1, Convict => 1,
	       Date => 1, "Declare-Bankruptcy" => 1, Demonstrate => 1, Die => 1, Elect => 1, "End-Org" => 1,
	       "End-Position" => 1, Execute => 1, Extradite => 1, Fine => 1, "Get-Sick" => 1, "Get-Well" => 1,
	       "Hold-Hearing" => 1, Indict => 1, Injure => 1, Interrupt => 1, Jail => 1, Meet => 1, "Merge-Org" => 1,
	       Nominate => 1, Observe => 1, Pardon => 1, Parole => 1, Prevent => 1, Release => 1, Sentence => 1,
	       Separate => 1, "Start-Org" => 1, "Start-Position" => 1, Sue => 1, "Transfer-Money" => 1,
	       "Transfer-Ownership" => 1, "Transport-Artifact" => 1, "Transport-Person" => 1, Try => 1, Witness => 1},
   TENSE => {Future => 1, Past => 1, Present => 1},
   TYPE => {Aspectual => {Interrupt => 1, Prevent => 1},
	    Business => {"Declare-Bankruptcy" => 1, "End-Org" => 1, "Merge-Org" => 1, "Start-Org" => 1},
	    Conflict => {Attack => 1, Demonstrate => 1},
	    Contact => {Communicate => 1, Meet => 1},
	    Justice => {Acquit => 1, Arrest => 1, Charge => 1, Convict => 1, Execute => 1, Extradite => 1,
			Fine => 1, "Hold-Hearing" => 1, Indict => 1, Jail => 1, Pardon => 1, Parole => 1,
			Release => 1, Sentence => 1, Sue => 1, Try => 1},
	    Life => {"Be-Born" => 1, Date => 1, Die => 1, "Get-Sick" => 1, "Get-Well" => 1, Injure => 1,
		     Marry => 1, Separate => 1},
	    Movement => {"Transport-Artifact" => 1, "Transport-Person" => 1},
	    Perceptual => {Observe => 1, Witness => 1},
	    Personnel => {Elect => 1, "End-Position" => 1, Nominate => 1, "Start-Position" => 1},
	    Transaction => {"Transfer-Money" => 1, "Transfer-Ownership" => 1}});
my @event_attributes = sort keys %event_attributes;

my %event_argument_roles = 
  (Adjudicator => 1, Agent => 1, Artifact => 1, Attacker => 1, Beneficiary => 1, Buyer => 1, Crime => 1,
   Defendant => 1, Destination => 1, Entity => 1, Giver => 1, Illness => 1, Instrument => 1, Money => 1,
   Org => 1, Origin => 1, Person => 1, Place => 1, Plaintiff => 1, Position => 1, Price => 1, Prosecutor => 1,
   Recipient => 1, Seller => 1, Sentence => 1, Target => 1, "Time-After" => 1, "Time-As-Of" => 1,
   "Time-At-Beginning" => 1, "Time-At-End" => 1, "Time-Before" => 1, "Time-Ending" => 1, "Time-Holds" => 1,
   "Time-Starting" => 1, "Time-Within" => 1, Transporter => 1, Vehicle => 1, Victim => 1);
my @event_argument_roles = sort keys %event_argument_roles;

my %event_mention_attributes =
  (ID => {},
   LEVEL => {SEN => 1});

#################################
# DEFAULT SCORING PARAMETERS:

my $epsilon = 1E-8;
my $required_precision = 1E-12;

#Entity scoring parameters
my %entity_type_wgt =
  (PER => 1.00,
   ORG => 1.00,
   SUB => 1.00,
   VEH => 1.00,
   WEA => 1.00,
   GPE => 1.00,
   LOC => 1.00,
   FAC => 1.00);
my %entity_class_wgt = 
  (SPC => 1.00,
   GEN => 0.00,
   NEG => 0.00,
   USP => 0.00);
my %entity_err_wgt = 
  (TYPE    => 0.50,
   SUBTYPE => 0.90,
   CLASS   => 0.75);
my $entity_fa_wgt = 0.75;
my %entity_mention_type_wgt =
  (NAM => 1.00,
   NOM => 0.50,
   PRE => 0.00,
   PRO => 0.10);
my %entity_mention_err_wgt =
  (TYPE  => 0.90,
   ROLE  => 0.90,
   STYLE => 0.90);
my $entity_mention_fa_wgt = 0.75;
my $entity_mention_ref_fa_wgt = 0.00;

#Relation scoring parameters
my %relation_type_wgt;
foreach my $type (keys %{$relation_attributes{TYPE}}) {
  $relation_type_wgt{$type} = 1.00;
}
my %relation_err_wgt =
  (TYPE    => 0.50,
   SUBTYPE => 0.90);
my $relation_fa_wgt = 0.75;
my $relation_argument_fa_wgt = 0.00;

#Event scoring parameters
my %event_type_wgt;
foreach my $type (keys %{$event_attributes{TYPE}}) {
  $event_type_wgt{$type} = 1.00;
}
my %event_modality_wgt;
foreach my $mode (keys %{$event_attributes{MODALITY}}) {
  $event_modality_wgt{$mode} = 1.00;
}
my %event_err_wgt =
  (TYPE       => 0.50,
   MODALITY   => 0.75,
   SUBTYPE    => 0.90,
   GENERICITY => 1.00,
   POLARITY   => 1.00,
   TENSE      => 1.00);
my $event_fa_wgt = 0.75;
my $event_argument_role_err_wgt = 0.75;
my $event_argument_fa_wgt = 0.50;

#Timex2 scoring parameters
my $timex2_detection_wgt = 0.10;
my %timex2_attribute_wgt =
  (TIMEX2_ANCHOR_DIR => 0.25,
   TIMEX2_ANCHOR_VAL => 0.50,
   TIMEX2_MOD        => 0.10,
   TIMEX2_SET        => 0.10,
   TIMEX2_VAL        => 1.00);
my $timex2_fa_wgt = 0.75;
my $timex2_mention_fa_wgt = 0.75;

#Quantity scoring parameters
my %quantity_type_wgt =
  ("Contact-Info" => 1.00,
   Crime          => 1.00,
   Illness        => 1.00,
   "Job-Title"    => 1.00,
   Numeric        => 1.00,
   Sentence       => 1.00);
my %quantity_err_wgt = 
  (TYPE    => 0.50,
   SUBTYPE => 0.90);
my $quantity_fa_wgt = 0.75;
my $quantity_mention_fa_wgt = 0.75;

#################################
# SCORING SCHEMES:

my %parameter_set =
  (DEFAULT => {},
  );

#################################
# MAPPING PARAMETERS:

#Mapping is subject to the following constraints:
#  * Each system output object may map to only one reference object.
#  * Each reference object may map to at most one system output object.
#  * An object is mapped only if it improves the overall score.
#  * The mappings are chosen to maximize the overall score.

# min_overlap is the minimum mutual fractional overlap allowed
# for a mention head or a name to be declared as successfully detected.
my $min_overlap = 0.3;		#minimum fractional overlap for mention detection
# min_text_match is the minimum fractional contiguous matching string length
# for a mention head or a name to be declared as successfully recognized.
my $min_text_match = 0.3;	#minimum fractional matching string length

# max_diff is the maximum extent difference allowed for names and
#mentions to be declared a "match".
#       max_diff_chars is the maximum extent difference
#       in characters for text sources.
my $max_diff_chars = 4;
#       max_diff_time is the maximum extent difference
#       in seconds for audio sources.
my $max_diff_time = 0.4;
#       max_diff_xy is the maximum extent difference
#       in centimeters for image mentions.
my $max_diff_xy = 0.4;

#################################
# GLOBAL DATA

my (%ref_database, %tst_database, %eval_docs, %sys_docs);
my ($input_file, $input_doc, $input_element, $fatal_input_error_header);
my (%mention_detection_statistics, %attribute_statistics, %role_confusion_statistics);
my (%mention_role_statistics, %mention_style_statistics);
my (%name_statistics);
my (%mapped_values, %mapped_document_values, %mention_map, %argument_map);
my (%relation_type_statistics, %relation_subtype_statistics);
my (%source_types, $source_type, $data_type);
my (%event_type_statistics, %event_modality_statistics, %argument_role_statistics);

my (@entity_mention_types, @entity_types, @entity_classes);
my (@relation_types, @event_types, @event_modalities, @quantity_types);

my @error_types = ("correct", "miss", "fa", "error");
my @xdoc_types = ("1", ">1");
my @entity_value_types = ("<=0.1", "0.1-0.3", "0.3-1.0", "1-3", "3-10", ">10");
my @entity_style_types = ("LITERAL", "METONYMIC");
my @entity_mention_count_types = ("1", "2", "3-4", "5-8", ">8");
my @relation_mention_count_types = ("1", ">1");
my @event_mention_count_types = ("1", ">1");
my @timex2_mention_count_types = ("1", ">1");
my @quantity_mention_count_types = ("1", ">1");
my @relation_arg_err_count_types = ("0", "1", ">1");
my @event_arg_err_count_types = ("0", "1", ">1");

my $max_string_length_to_print = 40;

my ($entity_serial_number, $relation_serial_number, $event_serial_number);
my ($score_bound, $max_delta);

my $usage = "\n\n$0 -r <ref_list> -t <tst_list> [-m <mode>] [-hsaed]\n\n".
  "Description:  This Perl program evaluates ACE system performance.\n".
  "\n".
  "Required arguments:\n".
  "  -R <ref_file> or -r <ref_list>\n".
  "     <ref_file> is a file containing ACE reference data in apf format\n".
  "     <ref_list> is a file containing a list of files containing ACE\n".
  "         reference data in apf format\n".
  "  -T <tst_file> or -t <tst_list>\n".
  "     <tst_file> is a file containing ACE system output data in apf format\n".
  "     <ref_list> is a file containing a list of files containing ACE\n".
  "         system output data in apf format\n".
  "\n".
  "Optional arguments:\n".
  "  -m <parameter_set_name> controls scoring mode by providing a selection of\n".
  "         different parameters (pre)defined in named parameter sets\n".
  "  -h prints this help message to STDOUT\n".
  "  -s prints ACE annotation data summary to STDOUT\n".
  "  -a prints ref/sys comparison for all data\n".
  "  -e prints ref/sys comparison for errorful data\n".
  "  -d prints document-level scores for all tasks\n".
  "\n";

use vars qw ($opt_r $opt_R $opt_t $opt_T $opt_x);
use vars qw ($opt_m $opt_h $opt_s $opt_a $opt_e $opt_d);

#################################
# MAIN

{
  my ($date, $time) = date_time_stamp();
  print "command line (run on $date at $time):  ", $0, " ", join(" ", @ARGV), "\n";
  use Getopt::Std;
  getopts ('r:R:t:T:m:hsaedx:');
  $min_text_match = $opt_x if defined $opt_x;
  die $usage if defined $opt_h;
  $opt_r xor $opt_R or die "Error in specifying ref data$usage";
  $opt_t xor $opt_T or die "Error in specifying tst data$usage";
  my $parameter_set = $opt_m ? $opt_m : "DEFAULT";
  select_parameter_set ($parameter_set);
  print_parameters ($parameter_set);

#read in the data
  my $t0 = (times)[0];
  get_data ("REF", \%ref_database, \%eval_docs, $opt_R, $opt_r);
  get_data ("TST", \%tst_database, \%sys_docs, $opt_T, $opt_t);
  check_docs ();
  print_documents ("REF", \%eval_docs) if $opt_s;
  print_documents ("TST", \%sys_docs) if $opt_s;
  my $t1 = (times)[0];

#compute values
  compute_element_values ($ref_database{quantities}, $tst_database{quantities}, \&quantity_mention_score, \&quantity_document_value);
  my $t2 = (times)[0];
  compute_element_values ($ref_database{timex2s}, $tst_database{timex2s}, \&quantity_mention_score, \&timex2_document_value);
  my $t3 = (times)[0];
  compute_element_values ($ref_database{entities}, $tst_database{entities}, \&entity_mention_score, \&entity_document_value);
  my $t4 = (times)[0];
  compute_releve_values ($ref_database{relations}, $tst_database{relations}, \&compute_relation_argument_map, \&relation_document_value);
  my $t5 = (times)[0];
  compute_releve_values ($ref_database{events}, $tst_database{events}, \&compute_event_argument_map, \&event_document_value);
  my $t6 = (times)[0];

#map objects
  map_objects ($ref_database{quantities}, $tst_database{quantities}, \&map_entity_mentions);
  my $t7 = (times)[0];
  map_objects ($ref_database{timex2s}, $tst_database{timex2s}, \&map_entity_mentions);
  my $t8 = (times)[0];
  map_objects ($ref_database{entities}, $tst_database{entities}, \&map_entity_mentions);
  my $t9 = (times)[0];
  map_objects ($ref_database{relations}, $tst_database{relations}, \&map_releve_arguments);
  my $t10 = (times)[0];
  map_objects ($ref_database{events}, $tst_database{events}, \&map_releve_arguments);
  my $t11 = (times)[0];

#evaluate entities
  if ((keys %{$ref_database{entities}})>0 and (keys %{$tst_database{entities}})>0) {
    if ($opt_s) {
      print "\n======== REF entities ========\n\n";
      print_entities (\%ref_database);
      print "\n======== TST entities ========\n\n";
      print_entities (\%tst_database);
    }
    print "\n======== entity mapping ========\n\n";
    print_entity_mapping (\%ref_database, \%tst_database) if $opt_a or $opt_e;
    print "\n======== entity scoring ========\n";
    print "\nEntity Detection and Recognition statistics:\n";
    score_entity_detection ();
    score_entity_attribute_recognition ();
    (my $detection_stats, my $role_stats, my $style_stats) = mention_recognition_stats ("entities");
    score_entity_mention_detection ($detection_stats);
    score_entity_mention_attribute_recognition ($role_stats, $style_stats);
  }
  my $t12 = (times)[0];

#evaluate relations
  if ((keys %{$ref_database{relations}})>0 and (keys %{$tst_database{relations}})>0) {
    if ($opt_s) {
      print "\n======== REF relations  ========\n\n";
      print_relations (\%ref_database);
      print "\n======== TST relations  ========\n\n";
      print_relations (\%tst_database);
    }
    if ($opt_a or $opt_e) {
      print "\n======== relation mapping ========\n\n";
      print_releve_mapping ("relation", \&print_relation_mention_mapping, \@relation_attributes);
    }
    print "\n======== relation scoring ========\n";
    print "\nRelation Detection and Recognition statistics:\n";
    score_relation_detection ();
    score_relation_attribute_recognition ();
  }
  my $t13 = (times)[0];

#evaluate events
  if ((keys %{$ref_database{events}})>0 and (keys %{$tst_database{events}})>0) {
    if ($opt_s) {
      print "\n======== REF events  ========\n\n";
      print_events (\%ref_database);
      print "\n======== TST events  ========\n\n";
      print_events (\%tst_database);
    }
    if ($opt_a or $opt_e) {
      print "\n======== event mapping ========\n\n";
      print_releve_mapping ("event", \&print_event_mention_mapping, \@event_attributes);
    }
    print "\n======== event scoring ========\n";
    print "\nEvent Detection and Recognition statistics:\n";
    score_event_detection ("event scoring");
    score_event_attribute_recognition ();
  }
  my $t14 = (times)[0];

#evaluate entity mentions without regard to entity affiliation
  $ref_database{entities} = $ref_database{mention_entities};
  $tst_database{entities} = $tst_database{mention_entities};
  compute_element_values ($ref_database{entities}, $tst_database{entities}, \&entity_mention_score, \&entity_document_value);
  my $t15 = (times)[0];
  map_objects ($ref_database{entities}, $tst_database{entities}, \&map_entity_mentions);
  my $t16 = (times)[0];
  if ((keys %{$ref_database{entities}})>0 and (keys %{$tst_database{entities}})>0) {
    if ($opt_s) {
      print "\n======== REF entities ========\n\n";
      print_entities (\%ref_database);
      print "\n======== TST entities ========\n\n";
      print_entities (\%tst_database);
    }
    if ($opt_a or $opt_e) {
      print "\n======== entity mention mapping ========\n\n";
      print_entity_mapping (\%ref_database, \%tst_database, "entity mention mapping");
    }
    print "\n======== entity mention scoring ========\n";
    print "\nEntity Mention Detection statistics:\n";
    score_entity_detection ();
    score_entity_attribute_recognition ();
  }
  my $t17 = (times)[0];

#evaluate timex2 expressions
  if ((keys %{$ref_database{timex2s}})>0 and (keys %{$tst_database{timex2s}})>0) {
    if ($opt_s) {
      print "\n======== REF timexs ========\n\n";
      print_timex2s (\%ref_database) if $opt_s;
      print "\n======== TST timexs ========\n\n";
      print_timex2s (\%tst_database) if $opt_s;
    }
    if ($opt_a or $opt_e) {
      print "\n======== timex2 mapping ========\n\n";
      print_timex2_mapping (\%ref_database, \%tst_database);
    }
    print "\n======== timex2 scoring ========\n";
    print "\nTimex2 Detection and Recognition statistics:\n";
    score_timex2_detection ();
    my $attribute_stats = attribute_confusion_stats ("timex2s", \@timex2_attributes);
    foreach my $attribute (@timex2_attributes) {
      next if $attribute =~ /^(ID|TYPE)$/ or not $attribute_stats->{$attribute};
      print "\nRecognition statistics for attribute $attribute (for mapped elements):\n";
      score_confusion_stats ($attribute_stats->{$attribute});
    }
    (my $detection_stats) = mention_recognition_stats ("timex2s");
    score_timex2_mention_detection ();
  }
  my $t18 = (times)[0];

#evaluate quantity expressions
  if ((keys %{$ref_database{quantities}})>0 and (keys %{$tst_database{quantities}})>0) {
    if ($opt_s) {
      print "\n======== REF quantities ========\n\n";
      print_quantities (\%ref_database);
      print "\n======== TST quantities ========\n\n";
      print_quantities (\%tst_database);
    }
    if ($opt_a or $opt_e) {
      print "\n======== quantity mapping ========\n\n";
      print_quantity_mapping (\%ref_database, \%tst_database);
    }
    print "\n======== quantity scoring ========\n";
    print "\nQuantity Detection and Recognition statistics:\n";
    score_quantity_detection ();
    my $attribute_stats = attribute_confusion_stats ("quantities", \@quantity_attributes);
    foreach my $attribute (@quantity_attributes) {
      next if $attribute eq "ID";
      print "\nRecognition statistics for attribute $attribute (for mapped elements):\n";
      score_confusion_stats ($attribute_stats->{$attribute});
    }
    (my $detection_stats) = mention_recognition_stats ("quantities");
    score_quantity_mention_detection ();
  }
  my $t19 = (times)[0];

  printf "\ndata input:           %8.2f secs to load data\n", $t1-$t0;
  printf "entity eval:          %8.2f secs to compute values,%8.2f secs to map,%8.2f secs to score\n", $t4-$t3, $t9-$t8, $t12-$t11;
  printf "relation eval:        %8.2f secs to compute values,%8.2f secs to map,%8.2f secs to score\n", $t5-$t4, $t10-$t9, $t13-$t12;
  printf "event eval:           %8.2f secs to compute values,%8.2f secs to map,%8.2f secs to score\n", $t6-$t5, $t11-$t10, $t14-$t13;
  printf "entity mention eval:  %8.2f secs to compute values,%8.2f secs to map,%8.2f secs to score\n", $t15-$t14, $t16-$t15, $t17-$t16;
  printf "timex2 eval:          %8.2f secs to compute values,%8.2f secs to map,%8.2f secs to score\n", $t3-$t2, $t8-$t7, $t18-$t17;
  printf "quantity eval:        %8.2f secs to compute values,%8.2f secs to map,%8.2f secs to score\n", $t2-$t1, $t7-$t6, $t19-$t18;
}

#################################

sub get_data {

  my ($label, $db, $docs, $file, $list) = @_;

  if ($list) {
    open (LIST, $list) or die "\nUnable to open file list '$list'", $usage;
    while ($input_file = <LIST>) {
      chomp $input_file;
      get_document_data ($db, $docs, $input_file);
    }
    close (LIST);
  } else {
    $input_file = $file;
    get_document_data ($db, $docs, $input_file);
  }

#create reference pointers for all elements
  foreach my $element ("entity", "relation", "event", "timex2", "quantity", "mention_entity") {
    my $elements = $element."s";
    $elements =~ s/ys$/ies/;
    while ((my $id, my $ref) = each %{$db->{$elements}}) {
      not defined $db->{refs}{$id} or die
	"\n\nFATAL INPUT ERROR:  duplicate ID ($id) ".
	"for elements of type '$element' and '$db->{refs}{$id}{ELEMENT_TYPE}'\n\n";
      $db->{refs}{$id} = $ref;
      $ref->{ELEMENT_TYPE} = $element;
      while ((my $doc, my $mention) = each %{$ref->{documents}}) {
	foreach my $mention (@{$db->{$elements}{$id}{documents}{$doc}{mentions}}) {
	  not defined $db->{refs}{$mention->{ID}} or die
	    "\n\nFATAL INPUT ERROR:  duplicate ID ($mention->{ID}) ".
	    "for elements of type '$element mention' ".
	    "and '$db->{refs}{$mention->{ID}}{ELEMENT_TYPE}'\n\n";
	  $db->{refs}{$mention->{ID}} = $mention;
	  $mention->{ELEMENT_TYPE} = "$element mention";
	}
      }
    }
  }

  check_relation_arguments ($db);
  check_event_arguments ($db);
}

#################################

sub check_relation_arguments {

  my ($db) = @_;

  while ((my $id, my $relation) = each %{$db->{relations}}) {
    my %valid_arg_mention_ids;
    while ((my $role, my $arg) = each %{$relation->{arguments}}) {
      defined $db->{refs}{$arg->{ID}} or die
	"\n\nFATAL INPUT ERROR:  relation '$id' references argument '$arg->{ID}' in role '$role'\n".
	"    but this argument has not been loaded\n\n";
      foreach my $mention_id (element_mention_ids ($db, $arg->{ID})) {
	$valid_arg_mention_ids{$role}{$mention_id} = 1;
      }
    }
    while ((my $doc, my $doc_relation) = each %{$relation->{documents}}) {
      foreach my $mention (@{$doc_relation->{mentions}}) {
	while ((my $role, my $arg) = each %{$mention->{arguments}}) {
	  defined $db->{refs}{$arg->{ID}} or die
	    "\n\nFATAL INPUT ERROR:  relation mention '$mention->{ID}' references argument mention '$arg->{ID}' in role '$role'\n".
	    "    but this argument mention has not been loaded\n\n";
	  $valid_arg_mention_ids{$role}{$arg->{ID}} or die
	    "\n\nFATAL INPUT ERROR:  relation mention '$mention->{ID}' references argument mention '$arg->{ID}' in role '$role'\n".
	    "    but this argument mention is not a mention of any argument of relation '$id' in this role\n\n";
	}
      }
    }
  }
}

#################################

sub check_event_arguments {

  my ($db) = @_;

  while ((my $id, my $event) = each %{$db->{events}}) {
    my %valid_arg_mention_ids;
    while ((my $role, my $arg_ids) = each %{$event->{arguments}}) {
      foreach my $arg_id (keys %$arg_ids) {
	defined $db->{refs}{$arg_id} or die
	  "\n\nFATAL INPUT ERROR:  event '$id' references argument '$arg_id' in role '$role'\n".
	  "    but this argument has not been loaded\n\n";
	foreach my $mention_id (element_mention_ids ($db, $arg_id)) {
	  $valid_arg_mention_ids{$role}{$mention_id} = 1;
	}
      }
    }
    while ((my $doc, my $doc_event) = each %{$event->{documents}}) {
      foreach my $mention (@{$doc_event->{mentions}}) {
	while ((my $role, my $arg_ids) = each %{$mention->{arguments}}) {
	  foreach my $arg_id (keys %$arg_ids) {
	    defined $db->{refs}{$arg_id} or die
	      "\n\nFATAL INPUT ERROR:  event mention '$mention->{ID}' references argument mention '$arg_id' in role '$role'\n".
	      "    but this argument mention has not been loaded\n\n";
	    $valid_arg_mention_ids{$role}{$arg_id} or die
	      "\n\nFATAL INPUT ERROR:  event mention '$mention->{ID}' references argument mention '$arg_id' in role '$role'\n".
	      "    but this argument mention is not a mention of any argument of event '$id' in this role\n\n";
	  }
	}
      }
    }
  }
}

#################################

sub element_mention_ids {

  my ($db, $id) = @_;

  my @mention_ids;
  while ((my $doc, my $doc_element) = each %{$db->{refs}{$id}{documents}}) {
    foreach my $mention (@{$doc_element->{mentions}}) {
      push @mention_ids, $mention->{ID};
    }
  }
  return @mention_ids;
}

#################################

sub set_params {
  my ($prms, $data) = @_;

  while ((my $key, my $value) = each %$data) {
    $prms->{$key} = $value if defined $value;
  }
  my @sorted_names = sort {$prms->{$b} <=> $prms->{$a} ? $prms->{$b} <=> $prms->{$a} : $a cmp $b;} keys %$prms;
  return @sorted_names;
}

#################################

sub select_parameter_set {

  my ($name) = @_;
  my ($parms, $p, $type, $class);

  if (not defined ($parms = $parameter_set{$name})) {
    print STDERR "\n\nFATAL ERROR:  unknown parameter set name ($name)\n".
      "    available parameter set names are:\n";
    foreach $name (sort keys %parameter_set) {
      printf STDERR "        %s\n", $name;
    }
    die "\n";
  }

#Entity parameters
  @entity_mention_types = set_params (\%entity_mention_type_wgt, $parms->{entity_mention_type_wgt});
  @entity_types = set_params (\%entity_type_wgt, $parms->{entity_type_wgt});
  @entity_classes = set_params (\%entity_class_wgt, $parms->{entity_class_wgt});
  set_params (\%entity_err_wgt, $parms->{entity_err_wgt});
  set_params (\%entity_mention_err_wgt, $parms->{entity_mention_err_wgt});
  $entity_fa_wgt = $p if defined ($p = $parms->{entity_fa_wgt});
  $entity_mention_fa_wgt = $p if defined ($p = $parms->{entity_mention_fa_wgt});
  $entity_mention_ref_fa_wgt = $p if defined ($p = $parms->{entity_mention_ref_fa_wgt});

#Relation parameters
  @relation_types = set_params (\%relation_type_wgt, $parms->{relation_type_wgt});
  set_params (\%relation_err_wgt, $parms->{relation_err_wgt});
  $relation_fa_wgt = $p if defined ($p = $parms->{relation_fa_wgt});
  $relation_argument_fa_wgt = $p if defined ($p = $parms->{relation_argument_fa_wgt});

#Event parameters
  @event_types = set_params (\%event_type_wgt, $parms->{event_type_wgt});
  @event_modalities = set_params (\%event_modality_wgt, $parms->{event_modality_wgt});
  set_params (\%event_err_wgt, $parms->{event_err_wgt});
  $event_fa_wgt = $p if defined ($p = $parms->{event_fa_wgt});
  $event_argument_fa_wgt = $p if defined ($p = $parms->{event_argument_fa_wgt});
  $event_argument_role_err_wgt = $p if defined ($p = $parms->{event_argument_role_err_wgt});

#Timex2 parameters
  $timex2_detection_wgt = $p if defined ($p = $parms->{timex2_detection_wgt});
  set_params (\%timex2_attribute_wgt, $parms->{timex2_attribute_wgt});
  $timex2_fa_wgt = $p if defined ($p = $parms->{timex2_fa_wgt});
  $timex2_mention_fa_wgt = $p if defined ($p = $parms->{timex2_mention_fa_wgt});

#Quantity parameters
  @quantity_types = set_params (\%quantity_type_wgt, $parms->{quantity_type_wgt});
  $quantity_fa_wgt = $p if defined ($p = $parms->{quantity_fa_wgt});
  $quantity_mention_fa_wgt = $p if defined ($p = $parms->{quantity_mention_fa_wgt});
}

#################################

sub print_parameters {

  printf "PARAMETERS (scoring mode = $_[0])\n".
    "  min acceptable overlap of matching mention heads or names:\n".
    "%11.1f percent\n".
    "  min acceptable run of matching characters in mention heads:\n".
    "%11.1f percent\n".
    "  max acceptable extent difference for names and mentions to match:\n".
    "%11d chars for text sources\n".
    "%11.3f sec for audio sources\n".
    "%11.3f cm for image sources\n",
    100*$min_overlap, 100*$min_text_match, $max_diff_chars, $max_diff_time, $max_diff_xy;

#Entity parameters
  print "\n";
  print "  Entity mention values:\n";
  foreach my $type (@entity_mention_types) {
    printf "%11.3f for type %s\n", $entity_mention_type_wgt{$type}, $type;
  }
  print "  Entity value weights for entity types:\n";
  foreach my $type (@entity_types) {
    printf "%11.3f for type %s\n", $entity_type_wgt{$type}, $type;
  }
  print "  Entity value weights for entity classes:\n";
  foreach my $class (@entity_classes) {
    printf "%11.3f for class %s\n", $entity_class_wgt{$class}, $class;
  }
  print "  Entity value discounts for entity attribute recognition errors:\n";
  foreach my $type (sort keys %entity_err_wgt) {
    printf "%11.3f for $type errors\n", $entity_err_wgt{$type};
  }
  print "  Entity mention value discounts for mention attribute recognition errors:\n";
  foreach my $type (sort keys %entity_mention_err_wgt) {
    printf "%11.3f for $type errors\n", $entity_mention_err_wgt{$type};
  }
  printf "  Entity value (cost) weight for spurious (false alarm) entities:%6.3f\n", $entity_fa_wgt;
  printf "  Entity mention value (cost) weight for spurious entity mentions:%6.3f\n", $entity_mention_fa_wgt;
  printf "  Entity mention value (cost) discount for incorrect coreference:%6.3f\n", $entity_mention_ref_fa_wgt;

#Relation parameters
  print "\n";
  print "  Relation value weights for relation types:\n";
  foreach my $type (@relation_types) {
    printf "%11.3f for type %s\n", $relation_type_wgt{$type}, $type;
  }
  print "  Relation value discounts for relation attribute recognition errors:\n";
  foreach my $type (sort keys %relation_err_wgt) {
    printf "%11.3f for $type errors\n", $relation_err_wgt{$type};
  }
  printf "  Relation value (cost) weight for spurious (false alarm) relations:%6.3f\n", $relation_fa_wgt;
  printf "  Relation value (cost) weight for spurious relation arguments:%6.3f\n", $relation_argument_fa_wgt;

#Event parameters
  print "\n";
  print "  Event value weights for event types:\n";
  foreach my $type (@event_types) {
    printf "%11.3f for type %s\n", $event_type_wgt{$type}, $type;
  }
  print "  Event value weights for event modalities:\n";
  foreach my $type (@event_modalities) {
    printf "%11.3f for modality %s\n", $event_modality_wgt{$type}, $type;
  }
  print "  Event value discounts for event attribute recognition errors:\n";
  foreach my $type (sort keys %event_err_wgt) {
    printf "%11.3f for $type errors\n", $event_err_wgt{$type};
  }
  printf "  Event value (cost) weight for spurious (false alarm) events:%6.3f\n", $event_fa_wgt;
  printf "  Event value (cost) weight for spurious event arguments:%6.3f\n", $event_argument_fa_wgt;
  printf "  Event argument value (cost) discount for argument role errors:%6.3f\n", $event_argument_role_err_wgt;

#Timex2 parameters
  print "\n";
  print "  Timex2 attribute value weights for timex2 attributes:\n";
  foreach my $type (sort keys %timex2_attribute_wgt) {
    printf "%11.3f for type %s\n", $timex2_attribute_wgt{$type}, $type;
  }
  printf "%11.3f for timex2 detection\n", $timex2_detection_wgt;
  printf "  Timex2 value (cost) weight for spurious (false alarm) timex2's:%6.3f\n", $timex2_fa_wgt;
  printf "  Timex2 mention value (cost) weight for spurious timex2 mentions:%6.3f\n", $timex2_mention_fa_wgt;

#Quantity parameters
  print "\n";
  print "  Quantity value weights for quantity types:\n";
  foreach my $type (@quantity_types) {
    printf "%11.3f for type %s\n", $quantity_type_wgt{$type}, $type;
  }
  print "  Quantity value discounts for quantity attribute recognition errors:\n";
  foreach my $type (sort keys %quantity_err_wgt) {
    printf "%11.3f for $type errors\n", $quantity_err_wgt{$type};
  }
  printf "  Quantity value (cost) weight for spurious (false alarm) quantity's:%6.3f\n", $quantity_fa_wgt;
  printf "  Quantity mention value (cost) weight for spurious quantity mentions:%6.3f\n", $quantity_mention_fa_wgt;
}

#################################

sub check_docs {

  my ($doc_id, $eval_doc, $sys_doc);

  foreach $doc_id (keys %eval_docs) {
    $eval_doc = $eval_docs{$doc_id};
    $sys_doc = $sys_docs{$doc_id};
    $sys_doc or warn
      "\n\nWARNING:  ref doc '$doc_id' has no corresponding tst doc\n\n";
    next unless $sys_doc;
    $sys_doc->{TYPE} eq $eval_doc->{TYPE} or die
      "\n\nFATAL ERROR:  different reference and system output data types for document '$doc_id'\n".
	"    ref type is '$eval_doc->{TYPE}' but system output type is '$sys_doc->{TYPE}'\n\n";
  }
}

#################################

sub score_entity_detection {

  conditional_performance ("entities", "entity", "type", "TYPE", \@entity_types);
  conditional_performance ("entities", "entity", "level", "LEVEL", \@entity_mention_types);
  conditional_performance ("entities", "entity", "value", "ENTITY VALUE", \@entity_value_types);
  conditional_performance ("entities", "mention", "count", "MENTION COUNT", \@entity_mention_count_types);
  conditional_performance ("entities", "entity", "class", "CLASS", \@entity_classes);
  my @source_types = sort keys %source_types;
  conditional_performance ("entities", "source", "type", "SOURCE", \@source_types);
#  conditional_performance ("entities", "Ndoc", "type", "CROSS-DOC", \@xdoc_types, "TYPE", @entity_types);
  conditional_performance ("entities", "entity", "type", "TYPE", \@entity_types, undef, undef, 1);
  conditional_performance ("entities", "entity", "value", "ENTITY VALUE", \@entity_value_types, undef, undef, 1);
  my @subtypes = sort keys %{$entity_attributes{SUBTYPE}};
  conditional_performance ("entities", "entity", "subtype", "SUBTYPE", \@subtypes, "TYPE", \@entity_types);

  return unless $opt_d;
  (my $count, my $cost, my $nrm_cost, my $doc_costs) = conditional_error_stats ("entities", "TYPE");
  foreach my $doc (sort keys %$doc_costs) {
    my $ref_value = defined $doc_costs->{$doc}{REF} ? $doc_costs->{$doc}{REF} : 0;
    my $sys_value = $ref_value - (defined $doc_costs->{$doc}{SYS} ? $doc_costs->{$doc}{SYS} : 0);
    printf "%10.2f %s for $doc\n", ($ref_value ? 100*$sys_value/$ref_value : $sys_value), ($_[0] =~ /mention/ ? "EMD" : "EDR")." score";
  }
}

#################################

sub conditional_performance {

  my ($elements, $label1, $label2, $cond1, $c1s, $cond2, $c2s, $external_reconciliation) = @_;

  my $hdr1 = "________Count________     __________Count_(%)__________        ______________Cost_(%)________________       __Unconditioned_Cost_(%)_";
  my $hdr2 = "Ent   Detection   Rec     Detection   Rec    Unweighted        Detection   Rec   Value    Value-based       Max      Detection    Rec";
  my $hdr3 = "Tot    FA  Miss   Err      FA  Miss   Err    Pre--Rec--F        FA  Miss   Err     (%)    Pre--Rec--F      Value     FA   Miss    Err";

  (my $count, my $cost, my $nrm_cost) = conditional_error_stats ($elements, $cond1, $cond2, $external_reconciliation);

  if ($cond2) {
    foreach my $cond ("ALL", @$c2s) {
      next unless $count and $count->{$cond};
      print "\nEvaluation of externally reconciled elements:" if $external_reconciliation;
      print "\nPerformance statistics for $cond2 = $cond:";
      printf "\n ref      %s\n %-8s %s\n %-8s %s\n", $hdr1, $label1, $hdr2, $label2, $hdr3;
      foreach my $type (@$c1s, "total") {
	print_eval ($type, $count->{$cond}{$type}, $cost->{$cond}{$type}, $nrm_cost->{$cond}{$type}, $nrm_cost->{ALL}{total});
      }
    }
  } else {
    return unless %$count;
    print "\nEvaluation of externally reconciled elements:" if $external_reconciliation;
    printf "\n ref      %s\n %-8s %s\n %-8s %s\n", $hdr1, $label1, $hdr2, $label2, $hdr3;
    foreach my $type (@$c1s, "total") {
      print_eval ($type, $count->{$type}, $cost->{$type}, $nrm_cost->{$type}, $nrm_cost->{total});
    }
  }
}

#################################

sub print_eval {

  my ($type, $count, $cost, $ref_value, $total_value) = @_;

  return unless (defined $count->{correct} or
		 defined $count->{error} or
		 defined $count->{miss} or
		 defined $count->{fa});
  my $format = "%7.7s%6d%6d%6d%6d%8.1f%6.1f%6.1f%7.1f%5.1f%5.1f%8.1f%6.1f%6.1f%8.1f%7.1f%5.1f%5.1f%9.2f%7.2f%7.2f%7.2f\n";

  foreach my $category ("correct", "error", "miss", "fa") {
    $count->{$category} = 0 unless defined $count->{$category};
    $cost->{$category} = 0 unless defined $cost->{$category};
  }
  $ref_value = 0 unless defined $ref_value;
  my $nref = $count->{correct}+$count->{error}+$count->{miss};
  my $nsys = $count->{correct}+$count->{error}+$count->{fa};
  my $pn = 100/max(1E-30, $nref);
  my $cn = 100/max(1E-30, $ref_value);

  my $recall = $count->{correct}/max($nref,1E-30);
  my $precision = $count->{correct}/max($nsys,1E-30);
  my $fmeasure = 2*$precision*$recall/max($precision+$recall, 1E-30);

  my $value_correct = $ref_value-$cost->{miss}-$cost->{error}-$cost->{correct};
  my $value_recall = max($value_correct,0)/max(1E-30, $ref_value);
  my $sys_value = $ref_value-$cost->{miss}+$cost->{fa};
  my $value_precision = max($value_correct,0)/max(1E-30, $sys_value);
  my $value_fmeasure = 2*$value_precision*$value_recall/max($value_precision+$value_recall, 1E-30);

  my $un = 100/max($total_value,1E-30);
  printf $format, $type, $nref, $count->{fa}, $count->{miss}, $count->{error},
  min(999.9,$pn*$count->{fa}), $pn*$count->{miss}, $pn*$count->{error}, 100*$precision, 100*$recall, 100*$fmeasure,
  min(999.9,$cn*$cost->{fa}), $cn*$cost->{miss}, $cn*($cost->{error}+$cost->{correct}),
  max(-999.9,$cn*($value_correct-$cost->{fa})), 100*$value_precision, 100*$value_recall, 100*$value_fmeasure,
  $un*$ref_value, min(999.99,$un*$cost->{fa}), $un*$cost->{miss}, $un*($cost->{error}+$cost->{correct});
}

#################################

sub score_relation_detection {

  conditional_performance ("relations", "relation", "type", "TYPE", \@relation_types);
#  conditional_performance ("relations", "mention", "count", "MENTION COUNT", \@relation_mention_count_types);
  my @source_types = sort keys %source_types;
  conditional_performance ("relations", "source", "type", "SOURCE", \@source_types);
  conditional_performance ("relations", "argument", "errors", "ARGUMENT ERRORS", \@relation_arg_err_count_types);
#  conditional_performance ("relations", "relation", "type", "TYPE", \@relation_types, "ARGUMENT ERRORS", \@relation_arg_err_count_types);
  my @subtypes = sort keys %{$relation_attributes{SUBTYPE}};
  conditional_performance ("relations", "relation", "subtype", "SUBTYPE", \@subtypes, "TYPE", \@relation_types);

  return unless $opt_d;
  (my $count, my $cost, my $nrm_cost, my $doc_costs) = conditional_error_stats ("relations", "TYPE");
  foreach my $doc (sort keys %$doc_costs) {
    my $ref_value = defined $doc_costs->{$doc}{REF} ? $doc_costs->{$doc}{REF} : 0;
    my $sys_value = $ref_value - defined $doc_costs->{$doc}{SYS} ? $doc_costs->{$doc}{SYS} : 0;
    printf "%10.2f %s for $doc\n", ($ref_value ? 100*$sys_value/$ref_value : $sys_value), ($_[0] =~ /mention/ ? "RMD" : "RDR")." score";
  }
}

#################################

sub score_event_detection {

  conditional_performance ("events", "event", "type", "TYPE", \@event_types);
  conditional_performance ("events", "modality", "type", "MODALITY", \@event_modalities);
#  conditional_performance ("events", "mention", "count", "MENTION COUNT", \@event_mention_count_types);
  my @source_types = sort keys %source_types;
  conditional_performance ("events", "source", "type", "SOURCE", \@source_types);
  conditional_performance ("events", "argument", "errors", "ARGUMENT ERRORS", \@event_arg_err_count_types);
#  conditional_performance ("events", "event", "type", "TYPE", \@event_types, "ARGUMENT ERRORS", \@event_arg_err_count_types);
  my @subtypes = sort keys %{$event_attributes{SUBTYPE}};
  conditional_performance ("events", "event", "subtype", "SUBTYPE", \@subtypes, "TYPE", \@event_types);

  return unless $opt_d;
  (my $count, my $cost, my $nrm_cost, my $doc_costs) = conditional_error_stats ("events", "TYPE");
  foreach my $doc (sort keys %$doc_costs) {
    my $ref_value = defined $doc_costs->{$doc}{REF} ? $doc_costs->{$doc}{REF} : 0;
    my $sys_value = $ref_value - defined $doc_costs->{$doc}{SYS} ? $doc_costs->{$doc}{SYS} : 0;
    printf "%10.2f %s for $doc\n", ($ref_value ? 100*$sys_value/$ref_value : $sys_value), ($_[0] =~ /mention/ ? "VMD" : "VDR")." score";
  }
}

#################################

sub score_timex2_detection {

  my @types = sort keys %source_types;
  conditional_performance ("timex2s", "source", "type", "SOURCE", \@types);

  return unless $opt_d;
  (my $count, my $cost, my $nrm_cost, my $doc_costs) = conditional_error_stats ("timex2s", "SOURCE");
  foreach my $doc (sort keys %$doc_costs) {
    my $ref_value = defined $doc_costs->{$doc}{REF} ? $doc_costs->{$doc}{REF} : 0;
    my $sys_value = $ref_value - defined $doc_costs->{$doc}{SYS} ? $doc_costs->{$doc}{SYS} : 0;
    printf "%10.2f %s for $doc\n", ($ref_value ? 100*$sys_value/$ref_value : $sys_value), "TIMEX2 score";
  }
}

#################################

sub score_quantity_detection {

  my @types = sort keys %source_types;
  conditional_performance ("quantities", "source", "type", "SOURCE", \@types);

  return unless $opt_d;
  (my $count, my $cost, my $nrm_cost, my $doc_costs) = conditional_error_stats ("quantities", "SOURCE");
  foreach my $doc (sort keys %$doc_costs) {
    my $ref_value = defined $doc_costs->{$doc}{REF} ? $doc_costs->{$doc}{REF} : 0;
    my $sys_value = $ref_value - defined $doc_costs->{$doc}{SYS} ? $doc_costs->{$doc}{SYS} : 0;
    printf "%10.2f %s for $doc\n", ($ref_value ? 100*$sys_value/$ref_value : $sys_value), "Quantity score";
  }
}

#################################

sub score_confusion_stats {

  my ($stats) = @_;
  my $maxdisplay = 8;

#display dominant confusion statistics
  my (%ref_count, %tst_count, %sort_count);
  my $ntot = my $nref = my $ncor = my $nfa = my $nmiss = 0;
#select attribute values that contribute the most confusions
  while ((my $ref_value, my $tst_stats) = each %$stats) {
    while ((my $tst_value, my $count) = each %$tst_stats) {
      $ref_count{$ref_value} += $count;
      $tst_count{$tst_value} += $count;
      $ntot += $count;
      $nref += $count unless $ref_value eq "<undef>";
      if ($tst_value eq $ref_value) {
	$sort_count{$ref_value} += $epsilon*$count;
	$ncor += $count unless $ref_value eq "<undef>";
      } else {
	$sort_count{$tst_value} += $count;
	$sort_count{$ref_value} += $count;
	$nfa += $count if $ref_value eq "<undef>";
	$nmiss += $count if $tst_value eq "<undef>";
      }
    }
  }
  my @display_values = sort {$sort_count{$b} <=> $sort_count{$a}} keys %sort_count;
  my $ndisplay = min($maxdisplay, scalar @display_values);
  splice (@display_values, $ndisplay);

  #tabulate confusion statistics for "other" attribute values
  my $others = "all others";
  foreach my $value (@display_values) {
    $stats->{$value}{$others} = $ref_count{$value};
    $stats->{$others}{$value} = $tst_count{$value};
  }
  $stats->{$others}{$others} = $ntot;
  my $display_count;
  foreach my $ref_value (@display_values) {
    foreach my $tst_value (@display_values) {
      my $count = $stats->{$ref_value}{$tst_value};
      next unless $count;
      $stats->{$ref_value}{$others} -= $count;
      $stats->{$others}{$tst_value} -= $count;
      $stats->{$others}{$others} -= $count;
      $display_count += $count;
    }
  }

  #output results
  my $nerr = $nfa+$nref-$ncor;
  my $nsub = $nref-$nmiss-$ncor;
  my $nsys = $ncor+$nsub+$nfa;
  $nref = max($nref,$epsilon);
  $nsys = max($nsys,$epsilon);
  my $pfa = $nfa/$nref;
  my $psub = $nsub/$nref;
  my $pmiss = $nmiss/$nref;
  my $perror = $nerr/$nref;
  my $recall = ($ncor+$nsub)/$nref;
  my $precision = ($ncor+$nsub)/$nsys;
  my $f = 2*$precision*$recall/max($precision+$recall,$epsilon);
  printf "    Summary (count/percent):  Nref=%d/%.1f%s, Nfa=%d/%.1f%s, Nmiss=%d/%.1f%s, Nsub=%d/%.1f%s, Nerr=%d/%.1f%s"
    .", P/R/F=%.1f%s/%.1f%s/%.1f%s\n",
    $nref, 100, "%", $nfa, min(999.9,100*$pfa), "%", $nmiss, 100*$pmiss, "%",
    $nsub, min(999.9,100*$psub), "%", $nerr, min(999.9,100*$perror), "%",
    100*$precision, "%", 100*$recall, "%", 100*$f, "%";
  print "    Confusion matrix for major error contributors (count/percent):\n        ref\\tst:";
  push @display_values, $others if $display_count != $ntot;
  foreach my $tst_value (@display_values) {
    printf "%11.11s ", $tst_value;
  }
  print "\n";
  foreach my $ref_value (@display_values) {
    printf "  %14.14s", $ref_value;
    foreach my $tst_value (@display_values) {
      my $count = $stats->{$ref_value}{$tst_value};
      printf "%s", $count ? 
	(sprintf "%6d/%4.1f%s", $count, min(99.9,100*$count/max($ntot,$epsilon)), "%") :
	"      -     ";
    }
    print "\n";
  }
}

#################################

sub score_timex2_mention_detection {

  my (%men_count, $type, $men_type, $rol_type, $sty_type, $err_type);
  my ($pn);

  #scoring conditioned on mention type
  undef %men_count;
  foreach $err_type (@error_types) {
    my $nent = $mention_detection_statistics{$err_type};
    $men_count    {total}{$err_type} += $nent ? $nent : 0;
  }
  print "\nTimex2 Mention Detection and EXACT Extent Recognition statistics (for mapped timex2's):\n",
  "         ____________count______________       ____________percent____________\n",
  "         Detection     Extent_Recognition      Detection     Extent_Recognition\n",
  "         miss    fa     miss   err  corr       miss    fa     miss   err  corr\n";
  foreach $type ("total") {
    $pn = 100/max($epsilon, $men_count{$type}{miss}+$men_count{$type}{error}+$men_count{$type}{correct});
    printf "%5.5s%8d%6d%9d%6d%6d%11.1f%6.1f%9.1f%6.1f%6.1f\n", $type,
    $men_count{$type}{miss}, $men_count{$type}{fa},
    $men_count{$type}{miss}, $men_count{$type}{error}, $men_count{$type}{correct},
    $pn*$men_count{$type}{miss}, min(999.9,$pn*$men_count{$type}{fa}),
    $pn*$men_count{$type}{miss}, $pn*$men_count{$type}{error}, $pn*$men_count{$type}{correct};
  }
}

#################################

sub score_quantity_mention_detection {

  my (%men_count, $type, $men_type, $rol_type, $sty_type, $err_type);
  my ($pn);

  #scoring conditioned on mention type
  undef %men_count;
  foreach $err_type (@error_types) {
    my $nent = $mention_detection_statistics{$err_type};
    $men_count    {total}{$err_type} += $nent ? $nent : 0;
  }
  print "\nQuantity Mention Detection and EXACT Extent Recognition statistics (for mapped quantities):\n",
    "         ____________count______________       ____________percent____________\n",
      "         Detection     Extent_Recognition      Detection     Extent_Recognition\n",
	"         miss    fa     miss   err  corr       miss    fa     miss   err  corr\n";
  foreach $type ("total") {
    $pn = 100/max($epsilon, $men_count{$type}{miss}+$men_count{$type}{error}+$men_count{$type}{correct});
    printf "%5.5s%8d%6d%9d%6d%6d%11.1f%6.1f%9.1f%6.1f%6.1f\n", $type,
      $men_count{$type}{miss}, $men_count{$type}{fa},
	$men_count{$type}{miss}, $men_count{$type}{error}, $men_count{$type}{correct},
	  $pn*$men_count{$type}{miss}, min(999.9,$pn*$men_count{$type}{fa}),
	    $pn*$men_count{$type}{miss}, $pn*$men_count{$type}{error}, $pn*$men_count{$type}{correct};
  }
}

#################################

sub score_entity_attribute_recognition {

#type attributes
  my ($type, $ref_type, %type_stats);
  my (%entity_type_total);
  my $attribute_stats = attribute_confusion_stats ("entities", \@entity_attributes);
  my $type_stats = $attribute_stats->{TYPE};
  foreach $ref_type (@entity_types) {
    foreach $type (@entity_types) {
      $type_stats{$ref_type}{$type} = $attribute_stats->{TYPE}{$ref_type}{$type} ?
	$attribute_stats->{TYPE}{$ref_type}{$type} : 0;
      $entity_type_total{$ref_type} += $type_stats{$ref_type}{$type};
    }
  }
  print "\nEntity Type confusion matrix for \"$source_type\" sources (for mapped entities):\n",
    "               ___________count___________        __________percent__________\n",
      "    ref\\tst:  ";
  foreach $type (@entity_types) {
    printf " %3.3s  ", $type;
  }
  print "   ";
  foreach $type (@entity_types) {
    printf "   %3.3s", $type;
  }
  print "\n";
  foreach $ref_type (@entity_types) {
    printf "  %3.3s       ", $ref_type;
    foreach $type (@entity_types) {
      printf "%6d", $type_stats{$ref_type}{$type};
    }
    print "     ";
    foreach $type (@entity_types) {
      printf "%6.1f", 100*$type_stats{$ref_type}{$type} /
	max($entity_type_total{$ref_type},1);
    }
    print "\n";
  }

#attribute confusion statistics
  foreach my $attribute (@entity_attributes) {
    next if $attribute eq "ID";
    print "\nRecognition statistics for attribute $attribute (for mapped elements):\n";
    score_confusion_stats ($attribute_stats->{$attribute});
  }

#class attributes
  my ($class, $ref_class);
  my (%entity_class_total);
  my $class_stats = $attribute_stats->{CLASS};
  foreach $ref_class (@entity_classes) {
    foreach $class (@entity_classes) {
      $class_stats->{$ref_class}{$class} = 0 unless defined $class_stats->{$ref_class}{$class};
      $entity_class_total{$ref_class} += $class_stats->{$ref_class}{$class};
    }
  }
  print "\nEntity Class confusion matrix for \"$source_type\" sources (for mapped entities):\n",
    "               __count__        _percent_\n",
      "    ref\\tst:  ";
  foreach $class (@entity_classes) {
    printf " %3.3s  ", $class;
  }
  print "   ";
  foreach $class (@entity_classes) {
    printf "   %3.3s", $class;
  }
  print "\n";
  foreach $ref_class (@entity_classes) {
    printf "%5.5s       ", $ref_class;
    foreach $class (@entity_classes) {
      printf "%6d", $class_stats->{$ref_class}{$class};
    }
    print "     ";
    foreach $class (@entity_classes) {
      printf "%6.1f", 100*$class_stats->{$ref_class}{$class} /
	max($entity_class_total{$ref_class},1);
    }
    print "\n";
  }

#name attributes
  my $name_stats = name_recognition_stats ("entities");
  foreach $type (@entity_types) {
    foreach my $err (@error_types) {
      $name_stats->{$type}{$err} = 0 unless defined $name_stats-> {$type}{$err};
    }
    $name_stats->{total}{miss} += $name_stats->{$type}{miss};
    $name_stats->{total}{fa} += $name_stats->{$type}{fa};
    $name_stats->{total}{correct} += $name_stats->{$type}{correct};
    $name_stats->{total}{error} += $name_stats->{$type}{error};
  }
  print "\nName Detection and Extent Recognition statistics (for mapped entities):\n",
  " ref     ____________count______________       ____________percent____________\n",
  " entity  Detection     Extent_Recognition      Detection     Extent_Recognition\n",
  " type    miss    fa     miss   err  corr       miss    fa     miss   err  corr\n";
  foreach $type (@entity_types, "total") {
    my $total = ($name_stats->{$type}{miss} +
		 $name_stats->{$type}{error} +
		 $name_stats->{$type}{correct});
    printf "%5.5s%8d%6d%9d%6d%6d%11.1f%6.1f%9.1f%6.1f%6.1f\n", $type,
    $name_stats->{$type}{miss}, $name_stats->{$type}{fa}, $name_stats->{$type}{miss},
    $name_stats->{$type}{error}, $name_stats->{$type}{correct},
    100*$name_stats->{$type}{miss}/max($total,1), 100*$name_stats->{$type}{fa}/max($total,1),
    100*$name_stats->{$type}{miss}/max($total,1), 100*$name_stats->{$type}{error}/max($total,1),
    100*$name_stats->{$type}{correct}/max($total,1);
  }
}

#################################

sub score_entity_mention_detection {

  my ($mention_stats) = @_;

  my (%men_count, $type, $men_type, $rol_type, $sty_type, $err_type);
  my ($pn);

  #scoring conditioned on mention type
  undef %men_count;
  foreach $men_type (@entity_mention_types) {
    foreach $rol_type (@entity_types) {
      foreach $sty_type (@entity_style_types) {
	foreach $err_type (@error_types) {
	  my $nent = $mention_stats->{$men_type}{$rol_type}{$sty_type}{$err_type};
	  $men_count{$men_type}{$err_type} += $nent ? $nent : 0;
	  $men_count    {total}{$err_type} += $nent ? $nent : 0;
	}
      }
    }
  }
  print "\nMention Detection and Extent Recognition statistics (for mapped entities):\n",
    " ref     ____________count______________       ____________percent____________\n",
      " mention Detection     Extent_Recognition      Detection     Extent_Recognition\n",
	" type    miss    fa     miss   err  corr       miss    fa     miss   err  corr\n";
  foreach $type (@entity_mention_types, "total") {
    $pn = 100/max($epsilon, $men_count{$type}{miss}+$men_count{$type}{error}+$men_count{$type}{correct});
    printf "%5.5s%8d%6d%9d%6d%6d%11.1f%6.1f%9.1f%6.1f%6.1f\n", $type,
    $men_count{$type}{miss}, $men_count{$type}{fa},
    $men_count{$type}{miss}, $men_count{$type}{error}, $men_count{$type}{correct},
    $pn*$men_count{$type}{miss}, min(999.9,$pn*$men_count{$type}{fa}),
    $pn*$men_count{$type}{miss}, $pn*$men_count{$type}{error}, $pn*$men_count{$type}{correct};
  }

  #scoring conditioned on mention style
  undef %men_count;
  foreach $men_type (@entity_mention_types) {
    foreach $rol_type (@entity_types) {
      foreach $sty_type (@entity_style_types) {
	foreach $err_type (@error_types) {
	  my $nent = $mention_stats->{$men_type}{$rol_type}{$sty_type}{$err_type};
	  $men_count{$sty_type}{$err_type} += $nent ? $nent : 0;
	  $men_count    {total}{$err_type} += $nent ? $nent : 0;
	}
      }
    }
  }
  print "\nMention Detection and Extent Recognition statistics (for mapped entities):\n",
    " ref     ____________count______________       ____________percent____________\n",
      " mention Detection     Extent_Recognition      Detection     Extent_Recognition\n",
	" style   miss    fa     miss   err  corr       miss    fa     miss   err  corr\n";
  foreach $type (@entity_style_types, "total") {
    $pn = 100/max($epsilon, $men_count{$type}{miss}+$men_count{$type}{error}+$men_count{$type}{correct});
    printf "%5.5s%8d%6d%9d%6d%6d%11.1f%6.1f%9.1f%6.1f%6.1f\n", $type,
    $men_count{$type}{miss}, $men_count{$type}{fa},
    $men_count{$type}{miss}, $men_count{$type}{error}, $men_count{$type}{correct},
    $pn*$men_count{$type}{miss}, min(999.9,$pn*$men_count{$type}{fa}),
    $pn*$men_count{$type}{miss}, $pn*$men_count{$type}{error}, $pn*$men_count{$type}{correct};
  }

  #scoring conditioned on mention role
  undef %men_count;
  foreach $men_type (@entity_mention_types) {
    foreach $rol_type (@entity_types) {
      foreach $sty_type (@entity_style_types) {
	foreach $err_type (@error_types) {
	  my $nent = $mention_stats->{$men_type}{$rol_type}{$sty_type}{$err_type};
	  $men_count{$rol_type}{$err_type} += $nent ? $nent : 0;
	  $men_count    {total}{$err_type} += $nent ? $nent : 0;
	}
      }
    }
  }
  print "\nMention Detection and Extent Recognition statistics (for mapped entities):\n",
  " ref     ____________count______________       ____________percent____________\n",
  " mention Detection     Extent_Recognition      Detection     Extent_Recognition\n",
  " role    miss    fa     miss   err  corr       miss    fa     miss   err  corr\n";
  foreach $type (@entity_types, "total") {
    $pn = 100/max($epsilon, $men_count{$type}{miss}+$men_count{$type}{error}+$men_count{$type}{correct});
    printf "%5.5s%8d%6d%9d%6d%6d%11.1f%6.1f%9.1f%6.1f%6.1f\n", $type,
    $men_count{$type}{miss}, $men_count{$type}{fa},
    $men_count{$type}{miss}, $men_count{$type}{error}, $men_count{$type}{correct},
    $pn*$men_count{$type}{miss}, min(999.9,$pn*$men_count{$type}{fa}),
    $pn*$men_count{$type}{miss}, $pn*$men_count{$type}{error}, $pn*$men_count{$type}{correct};
  }
}

#################################

sub score_entity_mention_attribute_recognition {

  my ($role_stats, $style_stats) = @_;

  # role attributes
  my ($role, $ref_role, $ent_type);
  my (%mention_role_count, %mention_role_total);

  print "\nMention Role confusion matrix for \"$source_type\" sources (for mapped mentions)\n",
    "  For all mapped mentions:\n",
      "               ___________count___________        __________percent__________\n",
	"    ref\\tst:  ";
  foreach $role (@entity_types) {
    printf " %3.3s  ", $role;
  }
  print "   ";
  foreach $role (@entity_types) {
    printf "   %3.3s", $role;
  }
  print "\n";
  foreach $ref_role (@entity_types) {
    foreach $role (@entity_types) {
      foreach $ent_type (@entity_types) {
	$role_stats->{ROLE}{$ent_type}{$ref_role}{$role} = 0 unless defined $role_stats->{ROLE}{$ent_type}{$ref_role}{$role};
	$mention_role_count{$ref_role}{$role} += $role_stats->{ROLE}{$ent_type}{$ref_role}{$role};
      }
      $mention_role_total{$ref_role} += $mention_role_count{$ref_role}{$role};
    }
    printf "%5.5s       ", $ref_role;
    foreach $role (@entity_types) {
      printf "%6d", $mention_role_count{$ref_role}{$role};
    }
    print "     ";
    foreach $role (@entity_types) {
      printf "%6.1f", 100*$mention_role_count{$ref_role}{$role} /
	max($mention_role_total{$ref_role},1);
    }
    print "\n";
  }

  foreach $ent_type (()) {         #(@entity_types) {
    print "  For mapped mentions whose entity is of type \"$ent_type\":\n",
      "               ___________count___________        __________percent__________\n",
	"    ref\\tst:  ";
    foreach $role (@entity_types) {
      printf " %3.3s  ", $role;
    }
    print "   ";
    foreach $role (@entity_types) {
      printf "   %3.3s", $role;
    }
    print "\n";
    foreach $ref_role (@entity_types) {
      $mention_role_total{$ref_role} = 0;
      foreach $role (@entity_types) {
	$mention_role_total{$ref_role} += $role_stats->{ROLE}{$ent_type}{$ref_role}{$role};
      }
      printf "%5.5s       ", $ref_role;
      foreach $role (@entity_types) {
	printf "%6d", $role_stats->{ROLE}{$ent_type}{$ref_role}{$role};
      }
      print "     ";
      foreach $role (@entity_types) {
	printf "%6.1f", 100*$role_stats->{ROLE}{$ent_type}{$ref_role}{$role} /
	  max($mention_role_total{$ref_role},1);
      }
      print "\n";
    }
  }

  # style attributes
  my ($style, $ref_style);
  my (%mention_style_total);
  foreach $ref_style (@entity_style_types) {
    foreach $style (@entity_style_types) {
      $style_stats->{STYLE}{$ref_style}{$style} = 0 unless defined $style_stats->{STYLE}{$ref_style}{$style};
      $mention_style_total{$ref_style} += $style_stats->{STYLE}{$ref_style}{$style};
    }
  }
  print "\nMention Style confusion matrix for \"$source_type\" sources (for mapped mentions):\n",
    "               __count__        _percent_\n",
      "    ref\\tst:  ";
  foreach $style (@entity_style_types) {
    printf " %3.3s  ", $style;
  }
  print "   ";
  foreach $style (@entity_style_types) {
    printf "   %3.3s", $style;
  }
  print "\n";
  foreach $ref_style (@entity_style_types) {
    printf "%5.5s       ", $ref_style;
    foreach $style (@entity_style_types) {
      printf "%6d", $style_stats->{STYLE}{$ref_style}{$style};
    }
    print "     ";
    foreach $style (@entity_style_types) {
      printf "%6.1f", 100*$style_stats->{STYLE}{$ref_style}{$style} /
	max($mention_style_total{$ref_style},1);
    }
    print "\n";
  }
}

#################################

sub score_relation_attribute_recognition {

  my $attribute_stats = attribute_confusion_stats ("relations", \@relation_attributes);

  # type attributes
  my ($type, $ref_type, %type_stats);
  my (%relation_type_total);
  foreach $ref_type (@relation_types) {
    foreach $type (@relation_types) {
      $type_stats{$ref_type}{$type} = defined $attribute_stats->{TYPE}{$ref_type}{$type} ?
	$attribute_stats->{TYPE}{$ref_type}{$type} : 0;
      $relation_type_total{$ref_type} += $type_stats{$ref_type}{$type};
    }
  }
  print "\nRelation Type confusion matrix for \"$source_type\" sources (for mapped relations):\n"
    ."             COUNT", "." x (6*(@relation_types-1)), "    PERCENT", "." x (6*(@relation_types-1)), "\n    ref\\tst:";
  foreach $type (@relation_types) {
    printf " %5.5s", $type;
  }
  print "     ";
  foreach $type (@relation_types) {
    printf " %5.5s", $type;
  }
  print "\n";
  foreach $ref_type (@relation_types) {
    printf "%11.11s ", $ref_type;
    foreach $type (@relation_types) {
      printf "%6d", $type_stats{$ref_type}{$type};
    }
    print "     ";
    foreach $type (@relation_types) {
      printf "%6.1f", 100*$type_stats{$ref_type}{$type} /
	max($relation_type_total{$ref_type},1);
    }
    print "\n";
  }

  # subtype attributes
  my $subtype_stats = subtype_confusion_stats ("relations");
  my ($subtype);
  foreach $ref_type (@relation_types) {
    my $stats = $subtype_stats->{$ref_type};
    next unless $stats;
    my (@subtypes, %subtype_total);
    @subtypes = sort keys %{$relation_attributes{TYPE}{$ref_type}};
    foreach $subtype (@subtypes) {
      foreach $type (@subtypes) {
	$stats->{$subtype}{$type} = 0 unless
	  defined $stats->{$subtype} and defined $stats->{$subtype}{$type};
	$subtype_total{$subtype} += $stats->{$subtype}{$type};
      }
    }
    printf "\nRelation Subtype confusion matrix for \"$source_type\" sources (for mapped relations):\n"
      ."  type=%-5.5s", $ref_type;
    print " COUNT", "." x (6*(@subtypes-1)), "    PERCENT", "." x (6*(@subtypes-1)), "\n    ref\\tst: ";
    foreach $subtype (@subtypes) {
      printf "%5.5s ", $subtype;
    }
    print "     ";
    foreach $subtype (@subtypes) {
      printf "%5.5s ", $subtype;
    }
    print "\n";
    foreach $subtype (@subtypes) {
      printf "%11.11s ", $subtype;
      foreach $type (@subtypes) {
	printf "%6d", $stats->{$subtype}{$type};
      }
      print "     ";
      foreach $type (@subtypes) {
	printf "%6.1f", 100*$stats->{$subtype}{$type} /
	  max($subtype_total{$subtype},1);
      }
      print "\n";
    }
  }

  foreach my $attribute (@relation_attributes) {
    next if $attribute eq "ID";
    print "\nRecognition statistics for attribute $attribute (for mapped elements):\n";
    score_confusion_stats ($attribute_stats->{$attribute});
  }
  my $role_stats = role_confusion_stats ("relations");
  print "\nRecognition statistics for argument ROLE (for mapped elements):\n";
  score_confusion_stats ($role_stats);
}

#################################

sub score_event_attribute_recognition {

  my $attribute_stats = attribute_confusion_stats ("events", \@event_attributes);

  # type attributes
  my ($type, $ref_type, %type_stats);
  my (%event_type_total);
  foreach $ref_type (@event_types) {
    foreach $type (@event_types) {
      $type_stats{$ref_type}{$type} = defined $attribute_stats->{TYPE}{$ref_type}{$type} ?
	$attribute_stats->{TYPE}{$ref_type}{$type} : 0;
      $event_type_total{$ref_type} += $type_stats{$ref_type}{$type};
    }
  }
  print "\nEvent Type confusion matrix for \"$source_type\" sources (for mapped events):\n"
    ."             COUNT", "." x (6*(@event_types-1)), "    PERCENT", "." x (6*(@event_types-1)), "\n    ref\\tst:";
  foreach $type (@event_types) {
    printf " %5.5s", $type;
  }
  print "     ";
  foreach $type (@event_types) {
    printf " %5.5s", $type;
  }
  print "\n";
  foreach $ref_type (@event_types) {
    printf "%11.11s ", $ref_type;
    foreach $type (@event_types) {
      printf "%6d", $type_stats{$ref_type}{$type};
    }
    print "     ";
    foreach $type (@event_types) {
      printf "%6.1f", 100*$type_stats{$ref_type}{$type} /
	max($event_type_total{$ref_type},1);
    }
    print "\n";
  }

  # modality attributes
  my ($modality, $ref_modality, %modality_stats);
  my (%event_modality_total);
  foreach $ref_modality (@event_modalities) {
    foreach $modality (@event_modalities) {
      $modality_stats{$ref_modality}{$modality} = defined $attribute_stats->{MODALITY}{$ref_modality}{$modality} ?
	$attribute_stats->{MODALITY}{$ref_modality}{$modality} : 0;
      $event_modality_total{$ref_modality} += $modality_stats{$ref_modality}{$modality};
    }
  }
  print "\nEvent Modality confusion matrix for \"$source_type\" sources (for mapped events):\n"
    ."             COUNT", "." x (6*(@event_modalities-1)), "    PERCENT", "." x (6*(@event_modalities-1)), "\n    ref\\tst:";
  foreach $modality (@event_modalities) {
    printf " %5.5s", $modality;
  }
  print "     ";
  foreach $modality (@event_modalities) {
    printf " %5.5s", $modality;
  }
  print "\n";
  foreach $ref_modality (@event_modalities) {
    printf "%11.11s ", $ref_modality;
    foreach $modality (@event_modalities) {
      printf "%6d", $modality_stats{$ref_modality}{$modality};
    }
    print "     ";
    foreach $modality (@event_modalities) {
      printf "%6.1f", 100*$modality_stats{$ref_modality}{$modality} /
	max($event_modality_total{$ref_modality},1);
    }
    print "\n";
  }

  foreach my $attribute (@event_attributes) {
    next if $attribute eq "ID";
    print "\nRecognition statistics for attribute $attribute (for mapped elements):\n";
    score_confusion_stats ($attribute_stats->{$attribute}, "attribute $attribute");
  }

  my $role_stats = role_confusion_stats ("events");
  print "\nRecognition statistics for argument ROLE (for mapped elements):\n";
  score_confusion_stats ($role_stats, "argument ROLE");
}

#################################

sub value_type {

  my ($value, $value_types) = @_;

  foreach my $value_type (@$value_types) {
    return $value_type if $value_type =~ /^>/;
    my $upper_value = $value_type;
    $upper_value =~ s/.*[^0-9\.]//;
    return $value_type if not defined $value or $value <= $upper_value;
  }
}

#################################

sub condition_value {

  my ($element, $doc, $condition) = @_;

  my $doc_element = $element->{documents}{$doc};
  my ($value, $count_types, $narg_errs);
  if ($condition eq "MENTION COUNT") {
    $count_types = ($element->{ELEMENT_TYPE} eq "entity" ?    \@entity_mention_count_types :
		    $element->{ELEMENT_TYPE} eq "relations" ? \@relation_mention_count_types :
		    $element->{ELEMENT_TYPE} eq "events" ?    \@event_mention_count_types :
		    $element->{ELEMENT_TYPE} eq "timex2s" ?   \@timex2_mention_count_types :
		                                              \@quantity_mention_count_types);
    $value = value_type (scalar @{$doc_element->{mentions}}, $count_types);
  } elsif ($condition eq "ARGUMENT ERRORS") {
    $count_types = ($element->{ELEMENT_TYPE} eq "relations" ? \@relation_arg_err_count_types :
		                                              \@event_arg_err_count_types);
    $narg_errs = $element->{MAP} ? num_argument_mapping_errors ($element) : 0;
    $value = value_type ($narg_errs, $count_types);
  } else {
    $value =
      $condition eq "TYPE"         ? $element->{TYPE} :
      $condition eq "SUBTYPE"      ? $element->{SUBTYPE} :
      $condition eq "CLASS"        ? $element->{CLASS} :
      $condition eq "MODALITY"     ? $element->{MODALITY} :
      $condition eq "SOURCE"       ? $doc_element->{SOURCE} :
      $condition eq "LEVEL"        ? $doc_element->{LEVEL} :
      $condition eq "ENTITY VALUE" ? value_type ($doc_element->{VALUE}, \@entity_value_types) :
      $condition eq "CROSS-DOC"    ? value_type (scalar @{$element->{documents}}, \@xdoc_types) :
      undef;
  }
  defined $value or die
    "\n\nFATAL ERROR:  unknown condition ($condition) in call to condition_value\n\n";
  return $value ? $value : "<null>";
}

#################################

sub conditional_error_stats {

  my ($elements, $cond1, $cond2, $external_reconciliation) = @_;

  my $attributes =
    $elements eq "entities" ?  \@entity_attributes :
    $elements eq "relations" ? \@relation_attributes :
    $elements eq "events" ?    \@event_attributes :
    $elements eq "timex2s" ?   \@timex2_attributes :
                               \@quantity_attributes;
    
#accumulate statistics over all documents
  my (%error_count, %cumulative_cost, %normalizing_cost, %document_costs);
  while ((my $id, my $element) = each %{$ref_database{$elements}}) {
    next if ($external_reconciliation and not $element->{external_links});
    while ((my $doc, my $doc_ref) = each %{$element->{documents}}) {
      my $doc_tst=$doc_ref->{MAP};
      my $cost = my $norm_cost = $doc_ref->{VALUE};
      my ($err_type, $att_errs);
      if ($doc_tst) {
	$cost -= $mapped_document_values{$doc_ref->{ID}}{$doc_tst->{ID}}{$doc} if $doc_tst;
	foreach my $attribute (@$attributes) {
	  next if $attribute eq "ID";
	  next if not defined $doc_ref->{$attribute} and not defined $doc_tst->{$attribute};
	  $att_errs++ unless (defined $doc_ref->{$attribute} and defined $doc_tst->{$attribute}
			      and $doc_ref->{$attribute} eq $doc_tst->{$attribute});
	}
	my $arg_errs = num_argument_mapping_errors ($element)
	  if $element->{ELEMENT_TYPE} =~ /relation|event/;
	$err_type = ($att_errs or $arg_errs) ? "error" : "correct";
      } else {
	$err_type = "miss";
      }
      $err_type = match_external_links ($element, $element->{MAP}) if $external_reconciliation;
      my $c1_value = condition_value ($element, $doc, $cond1);
      if ($cond2) {
	my $c2_value = condition_value ($element, $doc, $cond2);
	foreach my $k2 ($c2_value, "ALL") {
	  foreach my $k1 ($c1_value, "total") {
	    $error_count{$k2}{$k1}{$err_type}++;
	    $cumulative_cost{$k2}{$k1}{$err_type} += $cost;
	    $normalizing_cost{$k2}{$k1} += $norm_cost;
	  }
	}
      } else {
	foreach my $k1 ($c1_value, "total") {
	  $error_count{$k1}{$err_type}++;
	  $cumulative_cost{$k1}{$err_type} += $cost;
	  $normalizing_cost{$k1} += $norm_cost;
	}
      }
      $document_costs{$doc}{REF} += $norm_cost;
      $document_costs{$doc}{SYS} += $cost;
    }
  }

#update entity false alarm statistics
  while ((my $id, my $element) = each %{$tst_database{$elements}}) {
    next if ($external_reconciliation and not $element->{external_links});
    while ((my $doc, my $doc_tst) = each %{$element->{documents}}) {
      next if $doc_tst->{MAP};
      my $norm_cost = 0;
      my $cost = -$doc_tst->{FA_VALUE};
      my $err_type = "fa";
      my $c1_value = condition_value ($element, $doc, $cond1);
      if ($cond2) {
	my $c2_value = condition_value ($element, $doc, $cond2);
	foreach my $k2 ($c2_value, "ALL") {
	  foreach my $k1 ($c1_value, "total") {
	    $error_count{$k2}{$k1}{$err_type}++;
	    $cumulative_cost{$k2}{$k1}{$err_type} += $cost;
	    $normalizing_cost{$k2}{$k1} += $norm_cost;
	  }
	}
      } else {
	foreach my $k1 ($c1_value, "total") {
	  $error_count{$k1}{$err_type}++;
	  $cumulative_cost{$k1}{$err_type} += $cost;
	  $normalizing_cost{$k1} += $norm_cost;
	}
      }
      $document_costs{$doc}{REF} += $norm_cost;
      $document_costs{$doc}{SYS} += $cost;
    }
  }
  return ({%error_count}, {%cumulative_cost}, {%normalizing_cost}, {%document_costs});
}

#################################

sub match_external_links { #return "correct" if any ref link matches any tst link

  my ($ref, $tst) = @_;

  if ($ref->{external_links}) {
    return "miss" if not $tst->{external_links};
  } else {
    return $tst->{external_links} ? "fa" : undef;
  }
  my %tst_links;
  foreach my $link (@{$tst->{external_links}}) {
    $tst_links{$link->{RESOURCE}} = $link->{ID};
  }
  foreach my $link (@{$ref->{external_links}}) {
    return "correct" if (defined $tst_links{$link->{RESOURCE}} and
			 $tst_links{$link->{RESOURCE}} eq $link->{ID});
  }
  return "error";
}

#################################

sub name_recognition_stats {

  my ($elements) = @_;

#accumulate statistics over all documents
  my %name_statistics;
  while ((my $id, my $element) = each %{$ref_database{$elements}}) {
    while ((my $doc, my $doc_ref) = each %{$element->{documents}}) {
      next unless my $doc_tst=$doc_ref->{MAP};
      my $ref_names = $doc_ref->{names};
      foreach my $ref_name (@$ref_names) {
	my $tst_name = $ref_name->{MAP};
	my $err_type = $tst_name ?
	  (extent_mismatch($ref_name->{locator}, $tst_name->{locator}) <= 1 ?
	   "correct" : "error") : "miss";
	$name_statistics{$element->{TYPE}}{$err_type}++;
      }
      my $tst_names = $doc_tst->{names};
      foreach my $tst_name (@$tst_names) {
	$name_statistics{$element->{TYPE}}{fa}++ unless $tst_name->{MAP};
      }
    }
  }
  return {%name_statistics};
}

#################################

sub mention_recognition_stats {

  my ($elements) = @_;

#accumulate statistics over all documents
  my (%detection_stats, %role_stats, %style_stats);
  while ((my $id, my $element) = each %{$ref_database{$elements}}) {
    while ((my $doc, my $doc_ref) = each %{$element->{documents}}) {
      next unless my $doc_tst=$doc_ref->{MAP};
      my $ref_mentions = $doc_ref->{mentions};
      my $tst_mentions = $doc_tst->{mentions};
      foreach my $ref_mention (@$ref_mentions) {
	my $err_type = "miss";
	my $ref_type = defined $ref_mention->{TYPE} ? $ref_mention->{TYPE} : $doc_ref->{TYPE};
	my $ref_role = defined $ref_mention->{ROLE} ? $ref_mention->{ROLE} : $doc_ref->{TYPE};
	my $ref_style = defined $ref_mention->{STYLE} ? $ref_mention->{STYLE} : "LITERAL";
	my $tst_mention = $ref_mention->{MAP};
	if ($tst_mention) {
	  my $tst_role = defined $tst_mention->{ROLE} ? $tst_mention->{ROLE} : $doc_tst->{TYPE};
	  my $tst_style = defined $tst_mention->{STYLE} ? $tst_mention->{STYLE} : "LITERAL";
	  $role_stats{ROLE}{$element->{TYPE}}{$ref_role}{$tst_role}++;
	  $style_stats{STYLE}{$ref_style}{$tst_style}++;
	  $err_type = extent_mismatch ($ref_mention->{extent}{locator}, $tst_mention->{extent}{locator}) <= 1 ?
	    "correct" : "error";
	}
	$detection_stats{$ref_type}{$ref_role}{$ref_style}{$err_type}++;
      }
      foreach my $tst_mention (@$tst_mentions) {
	next if $tst_mention->{MAP};
	my $tst_role = defined $tst_mention->{ROLE} ? $tst_mention->{ROLE} : $doc_tst->{TYPE};
	my $tst_style = defined $tst_mention->{STYLE} ? $tst_mention->{STYLE} : "LITERAL";
	$detection_stats{$tst_mention->{TYPE}}{$tst_role}{$tst_style}{fa}++;
      }
    }
  }
  return ({%detection_stats}, {%role_stats}, {%style_stats});
}

#################################

sub attribute_confusion_stats {

  my ($elements, $attributes) = @_;

#accumulate statistics over all documents
  my %attribute_stats;
  while ((my $id, my $element) = each %{$ref_database{$elements}}) {
    while ((my $doc, my $doc_ref) = each %{$element->{documents}}) {
      next unless my $doc_tst=$doc_ref->{MAP};
      foreach my $attribute (@$attributes) {
	next if $attribute eq "ID";
	next if not defined $doc_ref->{$attribute} and not defined $doc_tst->{$attribute};
	my $ref_att = $doc_ref->{$attribute} ? $doc_ref->{$attribute} : "<undef>";
	my $tst_att = $doc_tst->{$attribute} ? $doc_tst->{$attribute} : "<undef>";
	$attribute_stats{$attribute}{$ref_att}{$tst_att}++;
      }
    }
  }
  return {%attribute_stats};
}

#################################

sub subtype_confusion_stats {

  my ($elements) = @_;

#accumulate statistics over all documents
  my %stats;
  while ((my $id, my $element) = each %{$ref_database{$elements}}) {
    while ((my $doc, my $doc_ref) = each %{$element->{documents}}) {
      next unless my $doc_tst=$doc_ref->{MAP};
      my $ref_subtype = $doc_ref->{SUBTYPE} ? $doc_ref->{SUBTYPE} : "<undef>";
      my $tst_subtype = $doc_tst->{SUBTYPE} ? $doc_tst->{SUBTYPE} : "<undef>";
      $stats{$element->{TYPE}}{$ref_subtype}{$tst_subtype}++;
    }
  }
  return {%stats};
}

#################################

sub role_confusion_stats {

  my ($elements) = @_;

#accumulate statistics over all documents
  my %role_stats;
  while ((my $id, my $element) = each %{$ref_database{$elements}}) {
    while ((my $doc, my $doc_ref) = each %{$element->{documents}}) {
      next unless my $doc_tst=$doc_ref->{MAP};
      if ($elements eq "relations") {
	while ((my $ref_role, my $ref_arg) = each %{$doc_ref->{arguments}}) {
	  my $tst_arg = $ref_arg->{MAP};
	  my $tst_role = $tst_arg ? $tst_arg->{ROLE} : "<undef>";
	  $tst_role = $ref_role if ($ref_role =~ /Arg-[12]/ and
				    $tst_role =~ /Arg-[12]/ and
				    $relation_symmetry{$element->{TYPE}}{$element->{SUBTYPE}});
	  $role_stats{$ref_role}{$tst_role}++;
	}
      } else { #events
	while ((my $ref_role, my $ref_ids) = each %{$doc_ref->{arguments}}) {
	  foreach my $ref_arg (values %$ref_ids) {
	    my $tst_arg = $ref_arg->{MAP};
	    my $tst_role = $tst_arg ? $tst_arg->{ROLE} : "<undef>";
	    $role_stats{$ref_role}{$tst_role}++;
	  }
	}
	while ((my $tst_role, my $tst_ids) = each %{$doc_tst->{arguments}}) {
	  foreach my $tst_arg (values %$tst_ids) {
	    next if my $ref_arg = $tst_arg->{MAP};
	    $role_stats{"<undef>"}{$tst_role}++;
	  }
	}
      }
    }
  }
  return {%role_stats};
}

#################################

sub num_argument_mapping_errors {

  my ($ref) = @_;

  my $num_args = 0;
  if ($ref->{ELEMENT_TYPE} eq "relation") {
    $num_args += keys %{$ref->{arguments}};
  } elsif ($ref->{ELEMENT_TYPE} eq "event") {
    while ((my $ref_role, my $ref_ids) = each %{$ref->{arguments}}) {
      $num_args += keys %$ref_ids;
    }
  } else {
    die "\n\nFATAL ERROR in num_argument_mapped_errors - unknown element type ($ref->{ELEMENT_TYPE})\n\n";
  }
  
  return $num_args unless (defined $ref->{MAP} and
			   defined $argument_map{$ref->{ID}} and
			   defined $argument_map{$ref->{ID}}{$ref->{MAP}{ID}});

  my $num_correctly_mapped = 0;
  while ((my $tst_role, my $tst_ids) = each %{$argument_map{$ref->{ID}}{$ref->{MAP}{ID}}}) {
    while ((my $tst_id, my $ref_arg) = each %$tst_ids) {
      $num_correctly_mapped++ if ($ref_database{refs}{$ref_arg->{ID}}{MAP} and
				  $ref_database{refs}{$ref_arg->{ID}}{MAP} eq
				  $tst_database{refs}{$tst_id});
    }
  }
  return $num_args - $num_correctly_mapped;
}

#################################

sub print_entity_mapping {

  my ($ref_db, $tst_db) = @_;

  foreach my $ref_id (sort keys %{$ref_db->{entities}}) {
    my $print_data = $opt_a;
    my $output = "--------\n";
    my $ref_entity = $ref_db->{entities}{$ref_id};
    if (my $tst_entity = $ref_entity->{MAP}) {
      my $tst_id = $tst_entity->{ID};
      my $err_type = $ref_entity->{TYPE} eq $tst_entity->{TYPE} ? "" : "TYPE";
      $err_type .= ($err_type ? "/" : "")."CLASS" if $ref_entity->{CLASS} ne $tst_entity->{CLASS};
      $err_type = "  -- ENTITY $err_type MISMATCH" if $err_type;
      my $external_match = match_external_links ($ref_entity, $tst_entity);
      $err_type .= (defined $err_type ? ", " : " -- ")."EXTERNAL ID MISMATCH" if $external_match and $external_match ne "correct";
      $print_data ||= $opt_e if $err_type;
      $output .= ($err_type ? ">>> " : "    ")."ref entity ".id_plus_external_ids($ref_entity).
	sprintf (" (%3.3s/%3.3s/%3.3s)%s\n", $ref_entity->{TYPE}, $ref_entity->{LEVEL}, $ref_entity->{CLASS}, $err_type);
      $output .= ($err_type ? ">>> " : "    ")."tst entity ".id_plus_external_ids($tst_entity).
	sprintf (" (%3.3s/%3.3s/%3.3s)%s\n", $tst_entity->{TYPE}, $tst_entity->{LEVEL}, $tst_entity->{CLASS}, $err_type);
      $output .= sprintf ("      entity score:  %.5f out of %.5f\n",
			  $mapped_values{$ref_id}{$tst_id}, $ref_entity->{VALUE});
    } else {
      $print_data ||= $opt_e;
      $output .= sprintf (">>> ref entity %s (%3.3s/%3.3s/%3.3s) -- NO MATCHING TST ENTITY\n",
			  id_plus_external_ids($ref_entity), $ref_entity->{TYPE}, $ref_entity->{LEVEL}, $ref_entity->{CLASS});
      $output .= sprintf "      entity score:  0.00000 out of %.5f\n", $ref_entity->{VALUE};
    }
    foreach my $doc (sort keys %{$ref_entity->{documents}}) {
      next unless defined $eval_docs{$doc};
      my $doc_ref = $ref_entity->{documents}{$doc};
      print_entity_mapping_details ($doc_ref, $doc_ref->{MAP}, $doc, $print_data, $output);
      $output = "";
    }
  }

  return unless my $print_data = $opt_a or $opt_e;
  foreach my $tst_id (sort keys %{$tst_db->{entities}}) {
    my $tst_entity = $tst_db->{entities}{$tst_id};
    next if $tst_entity->{MAP};
    my $output = "--------\n";
    $output .= sprintf (">>> tst entity %s (%3.3s/%3.3s/%3.3s) -- NO MATCHING REF ENTITY\n",
			id_plus_external_ids($tst_entity), $tst_entity->{TYPE}, $tst_entity->{LEVEL}, $tst_entity->{CLASS});
    $output .= sprintf "      entity score:  %.5f out of 0.00000\n", $tst_entity->{FA_VALUE};
    foreach my $doc (sort keys %{$tst_entity->{documents}}) {
      next unless defined $eval_docs{$doc};
      my $doc_tst = $tst_entity->{documents}{$doc};
      next if $doc_tst->{MAP};
      print_entity_mapping_details (undef, $doc_tst, $doc, $print_data, $output);
      $output = "";
    }
  }
}

#################################

sub print_entity_mapping_details {

  my ($ref_entity, $tst_entity, $doc, $print_data, $output) = @_;
  my ($ref_mentions, $tst_mentions, $ref_mention, $tst_mention, $ref_names, $tst_names, $ref_name, $tst_name);
  my ($error_type, $text);
  my ($type, $mention, @mentions, $name, @names);

  (my $entity, my $max_value, my $value) =
    ($ref_entity and $tst_entity) ? ($ref_entity, $ref_entity->{VALUE}, $mapped_values{$ref_entity->{ID}}{$tst_entity->{ID}}) :
      ($ref_entity ? ($ref_entity, $ref_entity->{VALUE}, 0) :
       ($tst_entity, 0, $tst_entity->{FA_VALUE}));
  $output .= sprintf "- in document $doc:  score:  %.5f out of %.5f  (%3.3s/%3.3s/%3.3s)\n", $value, $max_value, $entity->{TYPE}, $entity->{LEVEL}, $entity->{CLASS};
  if ($ref_entity) {
    foreach $mention (@{$ref_entity->{mentions}}) {
      push @mentions, {DATA=>$mention, TYPE=>"REF"};
    }
    foreach $name (@{$ref_entity->{names}}) {
      push @names, {DATA=>$name, TYPE=>"REF"};
    }
  }
  if ($tst_entity) {
    foreach $mention (@{$tst_entity->{mentions}}) {
      push @mentions, {DATA=>$mention, TYPE=>"TST"};
    }
    foreach $name (@{$tst_entity->{names}}) {
      push @names, {DATA=>$name, TYPE=>"TST"};
    }
  }
  foreach $mention (sort compare_locators @mentions) {
    $type = $mention->{TYPE};
    $mention = $mention->{DATA};
    if ($mention->{MAP}) {
      next if $type eq "TST";
      $ref_mention = $mention;
      $tst_mention = $mention->{MAP};
      $text = defined $ref_mention->{head}{text} ? $ref_mention->{head}{text} : "???";
      $error_type = $ref_mention->{TYPE} eq $tst_mention->{TYPE} ? "" : "TYPE";
      $error_type .= $error_type ? "/ROLE" : "ROLE" if ($ref_mention->{ROLE} ne $tst_mention->{ROLE});
      $error_type .= $error_type ? "/STYLE" : "STYLE" if ($ref_mention->{STYLE} ne $tst_mention->{STYLE});
      if ($error_type) {
	$print_data ||= $opt_e;
	$output .= ">>>   ref mention=\"" . $text . "\"";
	$output .= sprintf " (%3.3s/%3.3s/%3.3s) -- MENTION $error_type MISMATCH (%3.3s/%3.3s/%3.3s)\n",
	$ref_mention->{TYPE}, $ref_mention->{ROLE}, $ref_mention->{STYLE},
	$tst_mention->{TYPE}, $tst_mention->{ROLE}, $tst_mention->{STYLE};
      } else {
	$output .= "      ref mention=\"" . $text . "\"";
	$output .= sprintf " (%3.3s/%3.3s/%3.3s)\n", $ref_mention->{TYPE}, $ref_mention->{ROLE}, $ref_mention->{STYLE};
      }
      if (extent_mismatch ($ref_mention->{extent}{locator}, $tst_mention->{extent}{locator}) > 1) {
	$print_data ||= $opt_e;
	$text = defined $ref_mention->{extent}{text} ? $ref_mention->{extent}{text} : "???";
	$output .= ">>>    ref mention extent = \"$text\"\n";
	$text = defined $tst_mention->{extent}{text} ? $tst_mention->{extent}{text} : "???";
	$output .= ">>>    tst mention extent = \"$text\" -- MENTION EXTENT MISMATCH\n";
      }
    } else {
      $print_data ||= $opt_e;
      $output .= ">>>   ".(lc$type)." mention=\"" . (defined $mention->{head}{text} ? $mention->{head}{text} : "???") . "\"";
      $output .= sprintf " (%3.3s/%3.3s/%3.3s) -- NO MATCHING %s MENTION\n",
      $mention->{TYPE}, $mention->{ROLE}, $mention->{STYLE}, ($type eq "REF"?"TST":"REF");
    }
  }
  foreach $name (sort compare_locators @names) {
    $type = $name->{TYPE};
    $name = $name->{DATA};
    next if $type eq "TST" and $name->{MAP};
    if ($name->{MAP}) {
      $ref_name = $name;
      $tst_name = $name->{MAP};
      if (extent_mismatch($ref_name->{locator}, $tst_name->{locator}) <= 1) {
	$output .= "         ref name=\"" . (defined $ref_name->{text} ? $ref_name->{text} : "???") . "\"\n";
      } else {
	$print_data ||= $opt_e;
	$text = defined $ref_name->{text} ? $ref_name->{text} : "???";
	$output .= ">>>    ref name extent = \"$text\"\n";
	$text = defined $tst_name->{text} ? $tst_name->{text} : "???";
	$output .= ">>>    tst name extent = \"$text\" -- NAME EXTENT MISMATCH\n";
      }
    } else {
      $print_data ||= $opt_e;
      $output .= ">>>      ".(lc$type)." name=\"" . (defined $name->{text} ? $name->{text} : "???") . "\"";
      $output .= " -- NO MATCHING ".($type eq "REF"?"TST":"REF")." NAME\n";
    }
  }
  print $output if $print_data;
}

#################################

sub print_relation_mention_mapping {

  my ($ref_relation, $tst_relation, $doc) = @_;

  my $ref_id = $ref_relation->{ID};
  my $tst_id = $tst_relation->{ID};
  my $doc_ref = $ref_relation ? $ref_relation->{documents}{$doc} : undef;
  my $doc_tst = $tst_relation ? $tst_relation->{documents}{$doc} : undef;
  my $ref_value = $doc_ref ? $doc_ref->{VALUE} : 0;
  my $tst_value = ($doc_ref ? ($doc_tst ? (defined $mapped_document_values{$ref_id}{$tst_id}{$doc} ? 
					   $mapped_document_values{$ref_id}{$tst_id}{$doc}
					   : 0)
			       : 0)
		   : $doc_tst->{FA_VALUE});
  printf "- in document $doc:  score:  %.5f out of %.5f\n", $tst_value, $ref_value;
}

#################################

sub relation_argument_description {

  my ($arg, $db, $error) = @_;

  my $id = $arg->{ID};
  my $role = $arg->{ROLE};
  my $ref = $db->{refs}{$id};
  my $type = $ref->{TYPE};
  my $out = (sprintf ("%6.6s (%3.3s/%3.3s/%3.3s/%3.3s): ID=%s", $role,
		      $ref->{TYPE}, $ref->{SUBTYPE} ? $ref->{SUBTYPE} : "---",
		      $ref->{LEVEL} ? $ref->{LEVEL} : "---", $ref->{CLASS} ? $ref->{CLASS} : "---", $ref->{ID}));
  if (my $text=longest_string($ref, "name")) {
    $out .= sprintf ", name=\"%s\"", $text;
  } elsif ($text=longest_string($ref, "mention", "head")) {
    $out .= sprintf ", head=\"%s\"", $text;
  } elsif ($text=longest_string($ref, "mention", "extent")) {
    $out .= sprintf ", extent=\"%s\"", $text;
  }
  $out .= $error ? " -- $error\n" : "\n";
  return $out;
}	

#################################

sub print_relation_mentions {

  my ($relation, $ref_refs) = @_;

  while ((my $doc, my $doc_relation) = each %{$relation->{documents}}) {
    print "      -- in document $doc\n";
    foreach my $mention (@{$doc_relation->{mentions}}) {
      print "         mention ID=$mention->{ID}";
      print $mention->{extent}{text} ? ", extent=\"$mention->{extent}{text}\"\n" : "\n";
      foreach my $role (sort keys %{$mention->{arguments}}) {
	my $ref = $ref_refs->{$mention->{arguments}{$role}{ID}};
	printf "%17s: ID=$ref->{ID}%s", $role, ($ref->{head} ? ", head=\"$ref->{head}{text}\"\n" :
						($ref->{extent} ? ", extent=\"$ref->{extent}{text}\"\n" : "\n"));
      }
    }
  }
}

#################################

sub compute_element_values {

  my ($refs, $tsts, $mention_scorer, $doc_scorer) = @_;

#compute self-values
  foreach my $element (values %$refs, values %$tsts) {
    while ((my $doc, my $doc_element) = each %{$element->{documents}}) {
      ($doc_element->{VALUE}) = &$doc_scorer ($element, undef, $doc);
      $element->{VALUE} += $doc_element->{VALUE};
    }
  }

#select putative REF-TST element pairs for mapping
  my (%doc_refs, %doc_tsts);
  while ((my $id, my $element) = each %$refs) {
    while ((my $doc, my $doc_element) = each %{$element->{documents}}) {
      push @{$doc_refs{$doc}}, $doc_element;
    }
  }
  while ((my $id, my $element) = each %$tsts) {
    while ((my $doc, my $doc_element) = each %{$element->{documents}}) {
      push @{$doc_tsts{$doc}}, $doc_element;
    }
  }

#compute mapped element values
  foreach my $doc (keys %doc_refs) {
    my @candidate_pairs = candidate_element_pairs ($doc_refs{$doc}, $doc_tsts{$doc}, $mention_scorer);
    foreach my $pair (@candidate_pairs) {
      my $ref_id = $pair->[0]{ID};
      my $tst_id = $pair->[1]{ID};
      (my $value, my $map) = &$doc_scorer ($ref_database{refs}{$ref_id}, $tst_database{refs}{$tst_id}, $doc);
      next unless defined $value;
      $mapped_document_values{$ref_id}{$tst_id}{$doc} = $value;
      $mapped_values{$ref_id}{$tst_id} += $value;
      $mention_map{$ref_id}{$tst_id}{$doc} = $map;
    }
  }

#compute fa-values
  foreach my $element (values %$refs, values %$tsts) {
    while ((my $doc, my $doc_element) = each %{$element->{documents}}) {
      ($doc_element->{FA_VALUE}) = &$doc_scorer (undef, $element, $doc);
      $element->{FA_VALUE} += $doc_element->{FA_VALUE};
    }
  }
}

#################################

sub candidate_element_pairs {

  my ($ref_doc_elements, $tst_doc_elements, $scorer) = @_;

  my @events;
  foreach my $element (@$ref_doc_elements) {
    foreach my $mention (@{$element->{mentions}}) {
      my $locator = $mention->{head} ? $mention->{head}{locator} : $mention->{extent}{locator};
      push @events, {TYPE => "REF", EVENT => "BEG", MENTION => $mention, ELEMENT => $element, LOCATOR => $locator};
      push @events, {TYPE => "REF", EVENT => "END", MENTION => $mention, ELEMENT => $element, LOCATOR => $locator};
    }
  }

  foreach my $element (@$tst_doc_elements) {
    foreach my $mention (@{$element->{mentions}}) {
      my $locator = $mention->{head} ? $mention->{head}{locator} : $mention->{extent}{locator};
      push @events, {TYPE => "TST", EVENT => "BEG", MENTION => $mention, ELEMENT => $element, LOCATOR => $locator};
      push @events, {TYPE => "TST", EVENT => "END", MENTION => $mention, ELEMENT => $element, LOCATOR => $locator};
    }
  }
  @events = sort compare_locator_events @events;

  my (%active_ref_events, %active_tst_events, %overlapping_mentions, %overlapping_elements, @output_pairs);
  foreach my $event (@events) {
    if ($event->{TYPE} eq "REF") {
      my $ref_event = $event;
      my $ref_mention = $ref_event->{MENTION};
      $event->{EVENT} eq "BEG" ? ($active_ref_events{$ref_mention} = $event) : (delete $active_ref_events{$ref_mention});
      foreach my $tst_event (values %active_tst_events) {
	my $tst_mention = $tst_event->{MENTION};
	next if defined $overlapping_mentions{$ref_mention}{$tst_mention};
	$overlapping_mentions{$ref_mention}{$tst_mention} = 1;
	my $score  = &$scorer ($ref_mention, $tst_mention);
	next unless $score;
	$ref_mention->{tst_scores}{$tst_mention} = $score;
	$tst_mention->{is_ref_mention} = 1;
	my $ref_element = $ref_event->{ELEMENT};
	my $tst_element = $tst_event->{ELEMENT};
	next if defined $overlapping_elements{$ref_element}{$tst_element};
	$overlapping_elements{$ref_element}{$tst_element} = 1;
	push @output_pairs, [$ref_element, $tst_element];
      }
    } else {
      my $tst_event = $event;
      my $tst_mention = $tst_event->{MENTION};
      $event->{EVENT} eq "BEG" ? ($active_tst_events{$tst_mention} = $event) : (delete $active_tst_events{$tst_mention});
      foreach my $ref_event (values %active_ref_events) {
	my $ref_mention = $ref_event->{MENTION};
	next if defined $overlapping_mentions{$ref_mention}{$tst_mention};
	$overlapping_mentions{$ref_mention}{$tst_mention} = 1;
	my $score  = &$scorer ($ref_mention, $tst_mention);
	next unless $score;
	$ref_mention->{tst_scores}{$tst_mention} = $score;
	$tst_mention->{is_ref_mention} = 1;
	my $ref_element = $ref_event->{ELEMENT};
	my $tst_element = $tst_event->{ELEMENT};
	next if defined $overlapping_elements{$ref_element}{$tst_element};
	$overlapping_elements{$ref_element}{$tst_element} = 1;
	push @output_pairs, [$ref_element, $tst_element];
      }
    }
  }
  return @output_pairs;
}

#################################

sub compare_locator_events {

  my $alocator = $a->{LOCATOR};
  my $blocator = $b->{LOCATOR};
  my $abeg = $a->{EVENT} eq "BEG";
  my $bbeg = $b->{EVENT} eq "BEG";
  my $cmp;

  if ($alocator->{data_type} eq "text" and $blocator->{data_type} eq "text") {
    $cmp = (($abeg ? $alocator->{start} : $alocator->{end}) <=>
	    ($bbeg ? $blocator->{start} : $blocator->{end}));
    return $cmp if $cmp;
  } elsif ($alocator->{data_type} eq "audio" and $blocator->{data_type} eq "audio") {
    $cmp = (($abeg ? $alocator->{tstart} : ($alocator->{tstart}+$alocator->{tdur})) <=> 
	    ($bbeg ? $blocator->{tstart} : ($blocator->{tstart}+$blocator->{tdur})));
    return $cmp if $cmp;
  } elsif ($alocator->{data_type} eq "image" and $blocator->{data_type} eq "image") {
    my $ax_box = $alocator->{bblist}[0];
    my $bx_box = $blocator->{bblist}[0];
    my $cmp = $ax_box->{page} <=> $bx_box->{page};
    return $cmp if $cmp;
    $cmp = (($abeg ? $ax_box->{y_start} : $ax_box->{y_start}+$ax_box->{height}) <=>
	    ($bbeg ? $bx_box->{y_start} : $bx_box->{y_start}+$bx_box->{height}));
    return $cmp if $cmp;
    $cmp = (($abeg ? $ax_box->{x_start} : $ax_box->{x_start}+$ax_box->{width}) <=>
	    ($bbeg ? $bx_box->{x_start} : $bx_box->{x_start}+$bx_box->{width}));
    return $cmp if $cmp;
  } else {
    die "\n\nFATAL ERROR in compare_locator_events\n\n";
  }
  $cmp = $a->{EVENT} cmp $b->{EVENT};
  return $cmp if $cmp;
  $cmp = $a->{TYPE} cmp $b->{TYPE};
  return $cmp;
}

#################################

sub map_objects {

  my ($ref_objects, $tst_objects, $map_internals) = @_;

  my %reversed_scores;
  while ((my $ref, my $hash) = each %mapped_values) {
    while ((my $tst, my $score) = each %$hash) {
      $reversed_scores{$tst}{$ref} = $score;
    }
  }

#group objects into cohort sets and map each set independently
  foreach my $object (values %$ref_objects) {
    next if exists $object->{cohort};
    my (@ref_cohorts, @tst_cohorts);
    get_cohorts ($object, \%mapped_values, \%reversed_scores, \@ref_cohorts, \@tst_cohorts, $ref_objects, $tst_objects);
    map_cohorts (\@ref_cohorts, \@tst_cohorts, \%mapped_values);

    foreach my $ref (@ref_cohorts) {
      my $tst = $ref->{MAP};
      &$map_internals ($ref, $tst) if $tst;
    }
  }
}

#################################

sub get_cohorts {
  my ($ref, $scores, $reversed_scores, $ref_cohorts, $tst_cohorts, $ref_db, $tst_db) = @_;

  my (%tst_map, %ref_map, @queue);
  @queue = ($ref->{ID}, 1);
  $ref->{cohort} = 1;
  $ref->{mapped} = 1;
  push @$ref_cohorts, $ref;
  $ref_map{$ref->{ID}} = 1;

  while (@queue > 0) {
    (my $id, my $ref_type) = splice @queue, 0, 2;
    if ($ref_type) { #find tst cohorts for this ref
      foreach my $tst_id (keys %{$scores->{$id}}) {
	next if defined $tst_map{$tst_id} or not defined $scores->{$id}{$tst_id};
	$tst_map{$tst_id} = 1;
	my $tst = $tst_db->{$tst_id};
	$tst->{cohort} = 1;
	$tst->{mapped} = 1;
	push @$tst_cohorts, $tst;
	splice @queue, scalar @queue, 0, $tst_id, 0;
      }
    } else { #find ref cohorts for this tst
      foreach my $ref_id (keys %{$reversed_scores->{$id}}) {
	next if defined $ref_map{$ref_id} or not defined $reversed_scores->{$id}{$ref_id};
	$ref_map{$ref_id} = 1;
	my $ref = $ref_db->{$ref_id};
	$ref->{cohort} = 1;
	$ref->{mapped} = 1;
	push @$ref_cohorts, $ref;
	splice @queue, scalar @queue, 0, $ref_id, 1;
      }
    }
  }
}

#################################

sub map_cohorts {

  my ($ref_cohorts, $tst_cohorts, $scores) = @_;

  my ($i, $j, $ref_id, $tst_id, %costs, $fa_value);

  #compute mapping costs
  for ($i=0; $i<@$ref_cohorts; $i++) {
    $ref_id = $ref_cohorts->[$i]{ID};
    for ($j=0; $j<@$tst_cohorts; $j++) {
      $tst_id = $tst_cohorts->[$j]{ID};
      $costs{$i}{$j} = $tst_cohorts->[$j]{FA_VALUE} - $scores->{$ref_id}{$tst_id} if
	defined $scores->{$ref_id}{$tst_id};
    }
  }
	    
  my $map = weighted_bipartite_graph_matching(\%costs) or die
    "\n\nFATAL ERROR:  Cohort mapping through BGM FAILED\n\n";

  foreach $i (keys %$map) {
    $j = $map->{$i};
    $ref_cohorts->[$i]{MAP} = $tst_cohorts->[$j];
    $tst_cohorts->[$j]{MAP} = $ref_cohorts->[$i];
  }
}

#################################

sub map_entity_mentions {

  my ($ref_entity, $tst_entity) = @_;

  foreach my $doc (keys %{$ref_entity->{documents}}) {
    my $ref_occ = $ref_entity->{documents}{$doc};
    my $tst_occ = $tst_entity->{documents}{$doc};
    next unless $tst_occ;
    $ref_occ->{MAP} = $tst_occ;
    $tst_occ->{MAP} = $ref_occ;

    #map mentions	    
    my $map = $mention_map{$ref_occ->{ID}}{$tst_occ->{ID}}{$doc};
    my $ref_mentions = $ref_occ->{mentions};
    my $tst_mentions = $tst_occ->{mentions};
    foreach my $i (keys %$map) {
      my $j = $map->{$i};
      $ref_mentions->[$i]{MAP} = $tst_mentions->[$j];
      $tst_mentions->[$j]{MAP} = $ref_mentions->[$i];
    }
	
    #map names
    my ($ref_names, $tst_names, $ref_name, $tst_name, $overlap, $max_overlap);
    $ref_names = $ref_occ->{names};
    $tst_names = $ref_occ->{MAP}{names};
    foreach $ref_name (@$ref_names) {
      $max_overlap = $min_overlap;
      foreach $tst_name (@$tst_names) {
	$overlap = span_overlap($ref_name->{locator}, $tst_name->{locator});
	if ($overlap > $max_overlap) {
	  $max_overlap = $overlap;
	  $ref_name->{MAP} = $tst_name;
	}
      }
      $tst_name = $ref_name->{MAP};
      $tst_name->{MAP} = $ref_name if $tst_name;
    }
  }
}

#################################

sub entity_document_value {
  
  my ($ref_entity, $tst_entity, $doc) = @_;

  my $fa_entity = not $ref_entity; #calculate FA score if ref is null
  $ref_entity = $tst_entity if not $ref_entity;
  $tst_entity = $ref_entity if not $tst_entity;
  my $ref_mentions = $ref_entity->{documents}{$doc}{mentions};
  my $tst_mentions = $tst_entity->{documents}{$doc}{mentions};

  if ($ref_entity eq $tst_entity) { #compute self-score
    my $mentions_score = 0;
    foreach my $mention (@$ref_mentions) {
      $mention->{self_score} = entity_mention_score ($mention, $mention);
      $mentions_score += $mention->{self_score} *
	($fa_entity ? ($mention->{is_ref_mention} ?
		       -$entity_mention_fa_wgt * $entity_mention_ref_fa_wgt :
		       -$entity_mention_fa_wgt) :
	 1) if $mention->{self_score};
    }
    my $entity_value = ($entity_type_wgt{$ref_entity->{TYPE}}*$entity_class_wgt{$ref_entity->{CLASS}});
    $entity_value *= $entity_fa_wgt if $fa_entity;
    $entity_value = max($entity_value, $epsilon);
    return $entity_value*$mentions_score;
  }

#find optimum mapping of ref mentions to tst mentions
  my (%mapping_costs, $mentions_score, $mentions_map, @fa_scores);
  for (my $j=0; $j<@$tst_mentions; $j++) {
    $fa_scores[$j] = -$tst_mentions->[$j]{self_score} * $entity_mention_fa_wgt;
    $fa_scores[$j] *= $entity_mention_ref_fa_wgt if $tst_mentions->[$j]{is_ref_mention};
    for (my $i=0; $i<@$ref_mentions; $i++) {
      next unless defined (my $tst_scores = $ref_mentions->[$i]{tst_scores});
      next unless defined (my $tst_score = $tst_scores->{$tst_mentions->[$j]});
      $mapping_costs{$j}{$i} = $fa_scores[$j] - $tst_score;
    }
  }
  return undef unless %mapping_costs;
  my ($map) = weighted_bipartite_graph_matching(\%mapping_costs) or die
    "\n\nFATAL ERROR:  Document level entity mention mapping through BGM FAILED\n\n";
  for (my $j=0; $j<@$tst_mentions; $j++) {
    next unless defined (my $i = $map->{$j});
    $mentions_map->{$i} = $j;
    $mentions_score += $fa_scores[$j] - $mapping_costs{$j}{$i};
  }
  return undef unless defined $mentions_score;

# compute entity value
  my $entity_value = min($entity_type_wgt{$ref_entity->{TYPE}}*$entity_class_wgt{$ref_entity->{CLASS}},
			 $entity_type_wgt{$tst_entity->{TYPE}}*$entity_class_wgt{$tst_entity->{CLASS}});
  $entity_value = max($entity_value,$epsilon);

#reduce value for errors in entity attributes
  while ((my $attribute, my $weight) = each %entity_err_wgt) {
    my $ref_attribute = $ref_entity->{$attribute};
    my $tst_attribute = $tst_entity->{$attribute};
    $entity_value *= $weight if (($ref_attribute xor $tst_attribute) or
				   (defined $tst_attribute and $ref_attribute ne $tst_attribute));
  }
  return $entity_value*$mentions_score, $mentions_map;
}

#################################

sub entity_mention_score {

#N.B.  The mention mapping score must be undef if tst doesn't match ref.

  my ($ref_mention, $tst_mention) = @_;

  my $score;
  if (defined $ref_mention->{head} and defined $tst_mention->{head}) {
    return undef if span_overlap($ref_mention->{head}{locator}, $ref_mention->{head}{locator}) < $min_overlap;
    return undef if text_match($ref_mention->{head}{text}, $tst_mention->{head}{text}) < $min_text_match;
    $score = min($entity_mention_type_wgt{$ref_mention->{TYPE}},
		 $entity_mention_type_wgt{$tst_mention->{TYPE}});
  } elsif (defined $ref_mention->{extent} and defined $tst_mention->{extent}) {
    return undef if span_overlap($ref_mention->{extent}{locator}, $ref_mention->{extent}{locator}) < $min_overlap;
    return undef if text_match($ref_mention->{extent}{text}, $tst_mention->{extent}{text}) < $min_text_match;
    $score = $epsilon;
  } else {
    return undef;
  }

#reduce value for errors in mention attributes
  while ((my $attribute, my $weight) = each %entity_mention_err_wgt) {
    $score *= $weight if $ref_mention->{$attribute} ne $tst_mention->{$attribute};
  }
  return $score;
}

#################################

sub text_match {

#This subroutine returns the maximum number of contiguous matching characters
#between two strings, expressed as a fraction of the maximum string length
  my $s1 = uc $_[0];
  my $s2 = uc $_[1];
    
#count only alphanumerics (because of legitimate variation in spaces and punctuation)
  $s1 =~ s/[^a-z0-9]//ig;
  $s2 =~ s/[^a-z0-9]//ig;
  return 0 unless $s1 and $s2;
  return 1 if $s1 eq $s2;

  ($s1, $s2) = ($s2, $s1) if length $s1 > length $s2;
  my @s1 = split //, $s1;
  my @s2 = split //, $s2;
  my $max_match = 0;
  while (@s1) {
    last if @s1 <= $max_match;
    for (my $n2=0; $n2<@s2; $n2++) {
      last if @s2-$n2 <= $max_match;
      my $n = 0;
      while ($s1[$n] eq $s2[$n2+$n]) {
	$n++;
	last if @s1 == $n or @s2 == $n2+$n;
      }
      $max_match = $n if $n > $max_match;
    }
    shift @s1;
  }
  return $max_match/max(length($s1),length($s2));
}
      
#################################

sub relation_document_value {

  my ($ref_relation, $tst_relation, $doc, $arg_map) = @_;

  my $fa_relation = not $ref_relation; #calculate FA score if ref is null
  $ref_relation = $tst_relation unless $ref_relation;
  $tst_relation = $ref_relation unless $tst_relation;
  my $ref_mentions = $ref_relation->{documents}{$doc}{mentions};
  my $tst_mentions = $tst_relation->{documents}{$doc}{mentions};
  $ref_mentions = () unless $ref_mentions;
  $tst_mentions = () unless $tst_mentions;

  if ($tst_relation eq $ref_relation) { #compute self-score
    my $arg_score = $ref_mentions ? $epsilon * @$ref_mentions : undef;
    my $arg_db = $fa_relation ? $tst_database{refs} : $ref_database{refs};
    while ((my $role, my $arg) = each %{$ref_relation->{arguments}}) {
      my $arg = $arg_db->{$arg->{ID}};
      $arg_score += $arg->{documents}{$doc}{VALUE} if defined $arg->{documents}{$doc}{VALUE};
    }
    my $relation_value = $relation_type_wgt{$ref_relation->{TYPE}};
    $relation_value *= -$relation_fa_wgt if $fa_relation;
    return $relation_value * $arg_score;
  }

#compute argument score
  (my $mentions_overlap, my $mentions_map) = optimum_relation_mentions_mapping ($ref_mentions, $tst_mentions);
  return undef unless $mentions_overlap or $arg_map;
  my $arg_score = $mentions_overlap ? $mentions_overlap * $epsilon : undef;
  while ((my $tst_role, my $tst_arg) = each %{$tst_relation->{arguments}}) {
    my $tst_id = $tst_arg->{ID};
    my $ref_arg = defined $arg_map->{$tst_role}{$tst_id} ? $arg_map->{$tst_role}{$tst_id} : undef;
    my $map_score = ($ref_arg and defined $mapped_document_values{$ref_arg->{ID}}{$tst_id}{$doc}) ?
      $mapped_document_values{$ref_arg->{ID}}{$tst_id}{$doc} : 0;
    $arg_score += $map_score
      + ($map_score - $tst_database{refs}{$tst_id}{documents}{$doc}{VALUE}) * $relation_argument_fa_wgt;
  }
  return undef unless defined $arg_score;

#compute relation value
  my $relation_value = min($relation_type_wgt{$ref_relation->{TYPE}},
			   $relation_type_wgt{$tst_relation->{TYPE}});

#reduce value for errors in relation attributes
  while ((my $attribute, my $weight) = each %relation_err_wgt) {
    my $ref_attribute = $ref_relation->{$attribute};
    my $tst_attribute = $tst_relation->{$attribute};
    $relation_value *= $weight if (($ref_attribute xor $tst_attribute) or
				   (defined $tst_attribute and $ref_attribute ne $tst_attribute));
  }
  return $relation_value*$arg_score, $mentions_map;
}

#################################

sub optimum_relation_mentions_mapping { #find the mapping that maximizes extent overlap

  my ($ref_mentions, $tst_mentions) = @_;

  my (%mapping_costs, $total_overlap, $mentions_map);
  for (my $i=0; $i<@$ref_mentions; $i++) {
    next unless defined $ref_mentions->[$i]{extent};
    for (my $j=0; $j<@$tst_mentions; $j++) {
      next unless defined $tst_mentions->[$j]{extent};
      my $score = span_overlap($ref_mentions->[$i]{extent}{locator},
			       $tst_mentions->[$j]{extent}{locator});
      $mapping_costs{$j}{$i} = -$score if $score > 0;
    }
  }
  return undef unless %mapping_costs;

  my ($map) = weighted_bipartite_graph_matching(\%mapping_costs) or die
    "\n\nFATAL ERROR:  Document level relation mention mapping through BGM FAILED\n\n";
  for (my $j=0; $j<@$tst_mentions; $j++) {
    next unless defined (my $i = $map->{$j});
    $mentions_map->{$i} = $j;
    $total_overlap -= $mapping_costs{$j}{$i};
  }
  return $total_overlap, $mentions_map;
}

#################################

sub compute_relation_argument_map {

  my ($ref_relation, $tst_relation) = @_;

#collect arguments
  my (@ref_args, @tst_args);
  while ((my $role, my $arg) = each %{$ref_relation->{arguments}}) {
    push @ref_args, {ID => $arg->{ID}, ROLE => $role};
  }
  return undef unless @ref_args;
  while ((my $role, my $arg) = each %{$tst_relation->{arguments}}) {
    push @tst_args, {ID => $arg->{ID}, ROLE => $role};
  }
  return undef unless @tst_args;

#compute argument mapping scores
  my %mapping_costs;
  for (my $i=0; $i<@ref_args; $i++) {
    my $ref_id = $ref_args[$i]->{ID};
    for (my $j=0; $j<@tst_args; $j++) {
      my $tst_id = $tst_args[$j]->{ID};
      next unless defined $mapped_values{$ref_id}{$tst_id};
      next unless ($tst_args[$j]->{ROLE} eq $ref_args[$i]->{ROLE} or
		   ($relation_symmetry{$ref_relation->{TYPE}}{$ref_relation->{SUBTYPE}} and
		    $ref_args[$i]->{ROLE} =~ /^Arg-[12]$/ and $tst_args[$j]->{ROLE} =~ /^Arg-[12]$/));
      my $map_score = my $sys_score = 0;
      foreach my $doc (keys %{$tst_relation->{documents}}) {
	$map_score += $mapped_document_values{$ref_id}{$tst_id}{$doc} if
	  defined $mapped_document_values{$ref_id}{$tst_id}{$doc};
	$sys_score += $tst_database{refs}{$tst_id}{VALUE};
      }
      my $score = $map_score
	+ ($map_score - $sys_score) * $relation_argument_fa_wgt;
      my $fa_score = -$sys_score * $relation_argument_fa_wgt;
      $mapping_costs{$j}{$i} = $fa_score - $score;
    }
  }
  return undef unless %mapping_costs;

#find optimum mapping
  my ($map) = weighted_bipartite_graph_matching(\%mapping_costs) or die
    "\n\nFATAL ERROR:  Relation argument mapping through BGM FAILED\n\n";

  my (%arg_map, $order);
  for (my $j=0; $j<@tst_args; $j++) {
    next unless defined (my $i = $map->{$j});
    $arg_map{$tst_args[$j]->{ROLE}}{$tst_args[$j]->{ID}} = {ROLE => $ref_args[$i]->{ROLE},
							    ID => $ref_args[$i]->{ID}};
    $order = $tst_args[$j]->{ROLE} eq $ref_args[$i]->{ROLE} ? "normal" : "reversed"
      if $tst_args[$j]->{ROLE} =~ /^Arg-[12]$/;
  }
  return undef unless defined $order;
  ($arg_map{"Arg-1"}{$tst_relation->{arguments}{"Arg-1"}{ID}},
   $arg_map{"Arg-2"}{$tst_relation->{arguments}{"Arg-2"}{ID}}) = $order eq "normal" ?
     ({ROLE => "Arg-1", ID => $ref_relation->{arguments}{"Arg-1"}{ID}},
      {ROLE => "Arg-2", ID => $ref_relation->{arguments}{"Arg-2"}{ID}}) :
      ({ROLE => "Arg-2", ID => $ref_relation->{arguments}{"Arg-2"}{ID}},
       {ROLE => "Arg-1", ID => $ref_relation->{arguments}{"Arg-1"}{ID}});
  return {%arg_map};
}

#################################

sub span_overlap { #This subroutine returns the minimum mutual overlap between two spans.

  my ($ref_locator, $tst_locator, $minmax) = @_;

  if ($ref_locator->{data_type} eq "text" and $tst_locator->{data_type} eq "text") {
    return text_span_overlap ($ref_locator, $tst_locator, $minmax);
  } elsif ($ref_locator->{data_type} eq "audio" and $tst_locator->{data_type} eq "audio") {
    return audio_span_overlap ($ref_locator, $tst_locator, $minmax);
  } elsif ($ref_locator->{data_type} eq "image" and $tst_locator->{data_type} eq "image") {
    return image_span_overlap ($ref_locator, $tst_locator, $minmax);
  } else {
    die "\n\nFATAL ERROR in span_overlap\n"
      ."    unknown or nonexistent or incompatible ref/tst locator data types\n\n";
  }
}

#################################

sub text_span_overlap { #This subroutine returns the minimum mutual overlap between two spans.

  my ($ref_locator, $tst_locator, $minmax) = @_;
  my ($ref_start, $ref_end, $tst_start, $tst_end, $overlap);
    
  $ref_start = $ref_locator->{start};
  $ref_end = $ref_locator->{end};
  $tst_start = $tst_locator->{start};
  $tst_end = $tst_locator->{end};

  $overlap = 1 + (min($ref_end,$tst_end) -
		  max($ref_start,$tst_start));

  $minmax = \&max unless defined $minmax;
  return $overlap <= 0 ? 0 : $overlap / &$minmax ($ref_end-$ref_start+1,
						  $tst_end-$tst_start+1);
}

#################################

sub audio_span_overlap { #This subroutine returns the minimum mutual overlap between two spans.

  my ($ref_locator, $tst_locator, $minmax) = @_;
  my ($ref_start, $ref_end, $tst_start, $tst_end, $overlap);
  my ($ref_overlap, $tst_overlap);
  my $nominal_frame_period = 0.01;
    
  $ref_start = $ref_locator->{tstart};
  $ref_end = $ref_start + $ref_locator->{tdur};
  $tst_start = $tst_locator->{tstart};
  $tst_end = $tst_start + $tst_locator->{tdur};

  $overlap = $nominal_frame_period + (min($ref_end,$tst_end) -
				      max($ref_start,$tst_start));

  $minmax = \&max unless defined $minmax;
  return $overlap <= 0 ? 0 : $overlap / &$minmax ($ref_end-$ref_start+$nominal_frame_period,
						  $tst_end-$tst_start+$nominal_frame_period);
}

#################################

sub image_span_overlap { #This subroutine returns the minimum mutual overlap between two image spans.

  my ($ref_locator, $tst_locator, $minmax) = @_;
  my $ref_boxes = $ref_locator->{bblist};
  my $tst_boxes = $tst_locator->{bblist};
  my ($ref_box, $tst_box);
  my $nominal_pixel_size = 1;	# = 1 cm???!!! no -- current scale is pixels rather than cm

  my $ref_area = 0;
  foreach $ref_box (@$ref_boxes) {
    $ref_area += ($ref_box->{width} + $nominal_pixel_size)
      * ($ref_box->{height} + $nominal_pixel_size);
  }

  my $tst_area = 0;
  foreach $tst_box (@$tst_boxes) {
    $tst_area += ($tst_box->{width} + $nominal_pixel_size)
      * ($tst_box->{height} + $nominal_pixel_size);
  }
    
  my $mutual_area = 0;
  foreach $ref_box (@$ref_boxes) {
    foreach $tst_box (@$tst_boxes) {
      next if ($ref_box->{page} ne $tst_box->{page});
      my $x1 = max($ref_box->{x_start},
		   $tst_box->{x_start});
      my $x2 = min($ref_box->{x_start}+$ref_box->{width},
		   $tst_box->{x_start}+$tst_box->{width});
      next if ($x1 > $x2);
      my $y1 = max($ref_box->{y_start},
		   $tst_box->{y_start});
      my $y2 = min($ref_box->{y_start}+$ref_box->{height},
		   $tst_box->{y_start}+$tst_box->{height});
      next if ($y1 > $y2);
      $mutual_area += (($x2 - $x1 + $nominal_pixel_size) *
		       ($y2 - $y1 + $nominal_pixel_size));
    }
  }

  #return the minimum mutual overlap
  $minmax = \&max unless defined $minmax;
  return $mutual_area / &$minmax ($ref_area,$tst_area);
}

#################################

sub extent_mismatch { #This subroutine returns the maximum mismatch in the extent of two locators

  my ($ref_locator, $tst_locator) = @_;
  if ($ref_locator->{data_type} eq "text" and $ref_locator->{data_type} eq "text") {
    return text_extent_mismatch ($ref_locator, $tst_locator);
  } elsif ($ref_locator->{data_type} eq "audio" and $ref_locator->{data_type} eq "audio") {
    return audio_extent_mismatch ($ref_locator, $tst_locator);
  } elsif ($ref_locator->{data_type} eq "image" and $ref_locator->{data_type} eq "image") {
    return image_extent_mismatch ($ref_locator, $tst_locator);
  } else {
    die "\n\nFATAL ERROR in extent_mismatch\n\n";
  }
}

#################################

sub text_extent_mismatch { #This subroutine returns the maximum mismatch in the character extent of two text streams

  my ($ref_locator, $tst_locator) = @_;
  my $extent_mismatch = 0;

  $extent_mismatch =
    max(abs($ref_locator->{start} - $tst_locator->{start}),
	abs($ref_locator->{end} - $tst_locator->{end}))/$max_diff_chars;
}

#################################

sub audio_extent_mismatch { #This subroutine returns the maximum mismatch in the time extent of two audio signals

  my ($ref_locator, $tst_locator) = @_;
  my $extent_mismatch = 0;
  my ($ref_start, $ref_end, $tst_start, $tst_end);

  $ref_start = $ref_locator->{tstart};
  $tst_start = $tst_locator->{tstart};
  $ref_end = $ref_start + $ref_locator->{tdur};
  $tst_end = $tst_start + $tst_locator->{tdur};

  $extent_mismatch =
    max(abs($ref_start - $tst_start),
	abs($ref_end - $tst_end))/$max_diff_time;
}

#################################

sub image_extent_mismatch { #This subroutine returns the maximum mismatch in the spatial extent of two images.

  my ($ref_locator, $tst_locator) = @_;
  my $ref_boxes = $ref_locator->{bblist};
  my $tst_boxes = $tst_locator->{bblist};
  my ($ref_box, $tst_box);
  my ($x_mismatch, $y_mismatch);
  my $huge_mismatch = 9999999;
  my $extent_mismatch = 0;

  foreach $ref_box (@$ref_boxes) {
    $x_mismatch = $y_mismatch = $huge_mismatch;
    foreach $tst_box (@$tst_boxes) {
      next if ($ref_box->{page} ne $tst_box->{page});
      $x_mismatch = min($x_mismatch,
			abs($ref_box->{x_start} - $tst_box->{x_start}),
			abs($ref_box->{x_start}+$ref_box->{width} -
			    $tst_box->{x_start}+$tst_box->{width}));
      $y_mismatch = min($y_mismatch,
			abs($ref_box->{y_start} - $tst_box->{y_start}),
			abs($ref_box->{y_start}+$ref_box->{height} -
			    $tst_box->{y_start}+$tst_box->{height}));
    }
    $extent_mismatch = max($extent_mismatch, $x_mismatch, $y_mismatch);
  }
  return $extent_mismatch/$max_diff_xy;
}

#################################

sub max {

  my ($max, $x);

  $max = shift @_;
  foreach $x (@_) {
    if ($x > $max) {
      $max = $x;
    }
  }
  return $max;
}

#################################

sub min {

  my ($min, $x);

  $min = shift @_;
  foreach $x (@_) {
    if ($x < $min) {
      $min = $x;
    }
  }
  return $min;
}

#################################

sub get_document_data {

  my ($db, $docs, $file) = @_;
  my ($tag, $data, $doc_data, $span);

  #read data from file
  open (FILE, $file) or die "\nUnable to open ACE data file '$input_file'", $usage;
  while (<FILE>) {
    $data .= $_;
  }
  close (FILE);

  my $ndocs;
  while (($tag, $doc_data, $data) = extract_sgml_tag_and_span ("source_file", $data)) {
    $data_type = lc demand_attribute ("source_file", "TYPE", $tag, {text=>1, audio=>1, image=>1});
    my $src_file = demand_attribute ("source_file", "URI", $tag);
    my $source = demand_attribute ("source_file", "SOURCE", $tag);
    $source_type = $source if not defined $source_type;
    $source_type = "MIXED" if $source ne $source_type;
    $source_types{$source} = 1;
	
    #get document data for all documents in the source file
    while (($tag, $span, $doc_data) = extract_sgml_tag_and_span ("document", $doc_data)) {
      my $doc_id = demand_attribute ("document", "DOCID", $tag);
      not defined $docs->{$doc_id} or die
	"\n\nFATAL INPUT ERROR:  document ID '$doc_id' in file '$input_file' has already been defined\n\n";
      $input_doc = $doc_id;
      $fatal_input_error_header =
	"\n\nFATAL INPUT ERROR in document '$input_doc' in file '$input_file'\n";

      $docs->{$doc_id}{SOURCE} = $source;
      $docs->{$doc_id}{FILE} = $src_file;
      $docs->{$doc_id}{TYPE} = $data_type unless exists $docs->{$doc_id}{TYPE};
      $data_type eq $docs->{$doc_id}{TYPE} or die
	"\n\nFATAL INPUT ERROR:  all data for a given document must be of the same type\n".
	  "        data of type '$docs->{$doc_id}{TYPE} was previously loaded for document '$doc_id'\n".
	    "        but now file '$input_file' contains data of type '$data_type'\n\n";
      $ndocs++;
	    
      #load entity data into database
      my @entities = get_entities ($span);
      foreach my $entity (@entities) {
	$db->{entities}{$entity->{ID}} = {} if not defined $db->{entities}{$entity->{ID}};
	my $db_entity = $db->{entities}{$entity->{ID}};
	$db_entity->{documents}{$doc_id} = {%$entity};
	$db_entity->{documents}{$doc_id}{SOURCE} = $source;
	$db_entity->{LEVEL} = $entity->{LEVEL} if not defined $db_entity->{LEVEL} or
	  $entity_mention_type_wgt{$entity->{LEVEL}} > $entity_mention_type_wgt{$db_entity->{LEVEL}};
	promote_external_links ($db_entity, $entity);
	foreach my $attribute (@entity_attributes) {
	  next unless defined $entity->{$attribute};
	  $db_entity->{$attribute} = $entity->{$attribute} unless defined $db_entity->{$attribute};
	  $entity->{$attribute} eq $db_entity->{$attribute} or die
	    "\n\nFATAL INPUT ERROR:  attribute value conflict for attribute '$attribute'"
	    ." for entity '$entity->{ID}' in document '$doc_id'\n"
	    ."    database value is '$db_entity->{$attribute}'\n"
	    ."    document value is '$entity->{$attribute}'\n\n";
	}
	promote_entity_mentions_to_mention_entities ($db, $doc_id, $db_entity);
      }

      #load relation data into database
      my @relations = get_relations ($span);
      foreach my $relation (@relations) {
	$db->{relations}{$relation->{ID}} = {} if not defined $db->{relations}{$relation->{ID}};
	my $db_relation = $db->{relations}{$relation->{ID}};
	$db_relation->{documents}{$doc_id} = {%$relation};
	$db_relation->{documents}{$doc_id}{SOURCE} = $source;
	foreach my $attribute (@relation_attributes) {
	  next unless defined $relation->{$attribute};
	  $db_relation->{$attribute} = $relation->{$attribute} unless defined $db_relation->{$attribute};
	  $relation->{$attribute} eq $db_relation->{$attribute} or die
	    "\n\nFATAL INPUT ERROR:  attribute value conflict for attribute '$attribute'"
	      ." for relation '$relation->{ID}' in document '$doc_id'\n"
		."    database value is '$db_relation->{$attribute}'\n"
		  ."    document value is '$relation->{$attribute}'\n\n";
	}
	$db_relation->{arguments} = {} if not defined $db_relation->{arguments};
	my $db_args = $db_relation->{arguments};
	while ((my $role, my $arg) = each %{$relation->{arguments}}) {
	  not defined $db_args->{$role} or ($db_args->{$role}{ID} eq $arg->{ID}) or die $fatal_input_error_header.
	    "    argument conflict:  $role arg is multiply defined as '$arg->{ID}' and '$db_args->{$role}{ID}'\n\n";
	  $db_args->{$role} = $arg;
	}
      }

      #load event data into database
      my @events = get_events ($span);
      foreach my $event (@events) {
	$db->{events}{$event->{ID}} = {} if not defined $db->{events}{$event->{ID}};
	my $db_event = $db->{events}{$event->{ID}};
	not $db_event->{documents}{$doc_id} or die
	  "\n\nFATAL INPUT ERROR:  event $event->{ID} multiply defined in document $doc_id\n\n";
	$db_event->{documents}{$doc_id} = {%$event};
	$db_event->{documents}{$doc_id}{SOURCE} = $source;
	foreach my $attribute (@event_attributes) {
	  next unless defined $event->{$attribute};
	  $db_event->{$attribute} = $event->{$attribute} unless defined $db_event->{$attribute};
	  $event->{$attribute} eq $db_event->{$attribute} or die
	    "\n\nFATAL INPUT ERROR:  attribute value conflict for attribute '$attribute'"
	      ." for event '$event->{ID}' in document '$doc_id'\n"
		."    database value is '$db_event->{$attribute}'\n"
		  ."    document value is '$event->{$attribute}'\n\n";
	}
	$db_event->{arguments} = {} if not defined $db_event->{arguments};
	while ((my $role, my $ids) = each %{$event->{arguments}}) {
	  while ((my $id, my $arg) = each %$ids) {
	    $db_event->{arguments}{$role}{$id} = $arg;
	  }
	}
      }

      #load timex2 data into database
      my @timex2s = get_timex2s ($span);
      foreach my $timex2 (@timex2s) {
	$db->{timex2s}{$timex2->{ID}} = {} if not defined $db->{timex2s}{$timex2->{ID}};
	my $db_timex2 = $db->{timex2s}{$timex2->{ID}};
	$db_timex2->{documents}{$doc_id} = {%$timex2};
	$db_timex2->{documents}{$doc_id}{SOURCE} = $source;
	foreach my $attribute (@timex2_attributes) {
	  next unless defined $timex2->{$attribute};
	  $db_timex2->{$attribute} = $timex2->{$attribute}
	    unless defined $db_timex2->{$attribute};
	  $timex2->{$attribute} eq $db_timex2->{$attribute} or die
	    "\n\nFATAL INPUT ERROR:  attribute value conflict for attribute '$attribute'"
	      ." for timex2 '$timex2->{ID}' in document '$doc_id'\n"
		."    database value is '$db_timex2->{$attribute}'\n"
		  ."    document value is '$timex2->{$attribute}'\n\n";
	}
      }

      #load quantity data into database
      my @quantities = get_quantities ($span);
      foreach my $quantity (@quantities) {
	$db->{quantities}{$quantity->{ID}} = {}	if not defined $db->{quantities}{$quantity->{ID}};
	my $db_quantity = $db->{quantities}{$quantity->{ID}};
	$db_quantity->{documents}{$doc_id} = {%$quantity};
	$db_quantity->{documents}{$doc_id}{SOURCE} = $source;
	foreach my $attribute (keys %quantity_attributes) {
	  next unless defined $quantity->{$attribute};
	  $db_quantity->{$attribute} = $quantity->{$attribute}
	    unless defined $db_quantity->{$attribute};
	  $quantity->{$attribute} eq $db_quantity->{$attribute} or die
	    "\n\nFATAL INPUT ERROR:  attribute value conflict for attribute '$attribute'"
	      ." for quantity '$quantity->{ID}' in document '$doc_id'\n"
		."    database value is '$db_quantity->{$attribute}'\n"
		  ."    document value is '$quantity->{$attribute}'\n\n";
	}
      }
    }
  }
  $ndocs or warn
    "\n\nWARNING:  file '$input_file' contains no documents\n\n";
}

#################################

sub promote_entity_mentions_to_mention_entities {

  my ($db, $doc, $entity) = @_;

  my $mention_number;
  my $doc_entity = $entity->{documents}{$doc};
  my @mentions = @{$doc_entity->{mentions}};
  my @names = @{$doc_entity->{names}};
  foreach my $mention (@mentions) {
    my $new_name;
    my $max_overlap = $min_overlap;
    foreach my $name (@names) {
      next unless defined $mention->{head};
      my $overlap = span_overlap ($name->{locator}, $mention->{head}{locator});
      next if $overlap < $max_overlap;
      $new_name = $name;
      $max_overlap = $overlap;
    }
    if ($new_name) {
      $mention->{TYPE} eq "NAM" or warn
	"\n\nWARNING:  NAME found for un-NAME mention $mention->{ID} (name = '$new_name->{text}')\n\n";
    } else {
      $mention->{TYPE} ne "NAM" or $mention->{STYLE} eq "METONYMIC" or warn
	"\n\nWARNING:  no NAME found for NAME mention $mention->{ID}\n\n";
      $new_name = $mention->{head} if
	$mention->{TYPE} eq "NAM" and $mention->{STYLE} eq "METONYMIC";
    }

    my $mention_entity = {%$entity};
    my $mention_doc_entity = {%$doc_entity};
    $mention_entity->{ID} = $mention_doc_entity->{ID} =
      sprintf "%s.%s SN%s", $entity->{ID}, $mention_number++, $entity_serial_number++;
    $mention_entity->{documents} = {};
    $mention_entity->{documents}{$doc} = $mention_doc_entity;
    $mention = {%$mention};
    $mention->{ID} = "$mention->{ID}.$mention_number SN$entity_serial_number";
    $mention_entity->{documents}{$doc}{mentions} = [{%$mention}];
    $mention_entity->{documents}{$doc}{names} = $new_name ? [$new_name] : [];
    $db->{mention_entities}{$mention_entity->{ID}} = $mention_entity;
  }
  $mention_number or warn
    "\n\nWARNING:  no mentions found for entity '$entity->{ID}' in promote_entity_mentions_to_mention_entities\n\n";
}

#################################

sub get_events { #extract document-level information for all events in the document

  my ($data) = @_;
  my (@events, %event_ids);
  my ($tag, $span, $type, $modality, $class, $attribute, $mention);

  while (($tag, $span, $data) = extract_sgml_tag_and_span ("event", $data)) {

    my %event;

#get event ID
    $event{ID} = $input_element = demand_attribute ("event", "ID", $tag);
    not defined $event_ids{$input_element} or die
      "\n\nFATAL ERROR:  multiple definitions of event '$input_element' in file '$input_file'\n";
    $event_ids{$input_element} = 1;
    $fatal_input_error_header =
      "\n\nFATAL INPUT ERROR for event '$input_element' in document '$input_doc' in file '$input_file'\n";

#get event attributes
    $event{TYPE} = demand_attribute ("event", "TYPE", $tag, $event_attributes{TYPE});
    $event{SUBTYPE} = demand_attribute ("event", "SUBTYPE", $tag, $event_attributes{TYPE}{$event{TYPE}});
    $event{MODALITY} = get_attribute ("event", "MODALITY", $tag, $event_attributes{MODALITY});
    $event{MODALITY} = "Unspecified" unless $event{MODALITY};
    $event{POLARITY} = get_attribute ("event", "POLARITY", $tag, $event_attributes{POLARITY});
    $event{GENERICITY} = get_attribute ("event", "GENERICITY", $tag, $event_attributes{GENERICITY});
    $event{TENSE} = get_attribute ("event", "TENSE", $tag, $event_attributes{TENSE});
    $event{arguments} = get_event_arguments ($span);
    $event{mentions} = get_event_mentions ($span);
    @{$event{mentions}} > 0 or die $fatal_input_error_header.
      "    At least one mention is required, but none was found\n\n";
    push @events, {%event};
  }
  return @events;
}

#################################

sub get_attribute {

  my ($element, $attribute, $tag, $required_values) = @_;

  my $value = extract_sgml_tag_attribute ($attribute, $tag);
  not defined $value or not defined $required_values or defined $required_values->{$value} or die
    $fatal_input_error_header.
    "    Unrecognized $element $attribute attribute (= '$value'), $element tag is '$tag'\n\n";
  return $value;
}

#################################

sub demand_attribute {

  my ($element, $attribute, $tag, $required_values) = @_;

  my $value = extract_sgml_tag_attribute ($attribute, $tag);
  (defined $value and (not defined $required_values or defined $required_values->{$value})) or
   (not defined $value and defined $required_values and keys %$required_values == 0) or die
   $fatal_input_error_header.(defined $value ?
			      "    Illegal $element $attribute attribute (= '$value')" :
			      ("    Missing $element $attribute attribute").
			      ", $element tag is '".($tag ? $tag : "")."'\n\n");
  return $value;
}

#################################

sub get_event_arguments {

  my ($data) = @_;

  my %arguments;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("event_argument", $data)) {
    my %argument;
    $argument{ID} = demand_attribute ("event_argument", "REFID", $tag);
    $argument{ROLE} = demand_attribute ("event_argument", "ROLE", $tag, \%event_argument_roles);
    not defined $arguments{$argument{ROLE}}{$argument{ID}} or die $fatal_input_error_header.
      "    Multiple entries for argument '$argument{ID} in ROLE = '$argument{ROLE}'\n\n";
    $arguments{$argument{ROLE}}{$argument{ID}} = {%argument};
  }
  return {%arguments};
}

#################################

sub get_event_mentions {

  my ($data) = @_;
    
  my @mentions;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("event_mention", $data)) {
    my %mention;
    $mention{ID} = demand_attribute ("event", "ID", $tag);
    $mention{LEVEL} = get_attribute ("event", "LEVEL", $tag, $event_mention_attributes{LEVEL});
    $mention{arguments} = get_event_mention_arguments ($span);
    $mention{extent} = get_locator ("extent", $span);
    $mention{anchors} = get_event_mention_anchors ($span);
    @{$mention{anchors}} > 0 or die $fatal_input_error_header.
      "    No anchors found.  Event mention data is '$span'\n\n";
    push @mentions, {%mention};
  }
  @mentions > 0 or die $fatal_input_error_header."    No mentions found.\n\n";
  return [@mentions];
}

#################################

sub get_event_mention_arguments {

  my ($data) = @_;

  my %arguments;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("event_mention_argument", $data)) {
    my %argument;
    $argument{ID} = demand_attribute ("event_mention_argument", "REFID", $tag);
    $argument{ROLE} = demand_attribute ("event_mention_argument", "ROLE", $tag, \%event_argument_roles);
    not defined $arguments{$argument{ROLE}}{$argument{ID}} or die $fatal_input_error_header.
      "    Multiple entries for argument '$argument{ID}'\n\n";
    $arguments{$argument{ROLE}}{$argument{ID}} = {%argument};
  }
  return {%arguments};
}

#################################

sub get_event_mention_anchors {

  my ($data) = @_;

  my @anchors;
  while (my $anchor = get_locator ("anchor", $data)) {
    push @anchors, $anchor;
    (my $tag, my $span, $data) = extract_sgml_tag_and_span ("anchor", $data);
  }
  @anchors > 0 or die $fatal_input_error_header.
    "    At least one anchor is required but none was found\n\n";
  return [@anchors];
}

#################################

sub print_events {

  my ($db) = @_;

  foreach my $event_id (sort keys %{$db->{events}}) {
    my $event = $db->{events}{$event_id};
    printf "event ID=$event->{ID}, VALUE=%.5f, TYPE=%s, SUBTYPE=%s",
    $event->{VALUE}, $event->{TYPE}, $event->{SUBTYPE} ? $event->{SUBTYPE} : "<none>";
    foreach my $attribute (@event_attributes) {
      print ", $attribute=$event->{$attribute}" unless
	$attribute =~ /^(ID|TYPE|SUBTYPE)$/ or not defined $event->{$attribute};
    }
    print "\n";
    my @argument_info;
    foreach my $role (sort keys %{$event->{arguments}}) {
      foreach my $id (sort keys %{$event->{arguments}{$role}}) {
	print "  ".event_argument_description ($db, $event->{arguments}{$role}{$id});
      }
    }
    print_event_mentions ($event, $db->{refs});
  }
}

#################################

sub event_argument_description {

  my ($db, $argument, $text) = @_;

  my $id = $argument->{ID};
  my $ref = $db->{refs}{$id};
  my $out = sprintf ("%11.11s (%3.3s/%3.3s", $argument->{ROLE},
		      $ref->{TYPE}, $ref->{SUBTYPE} ? $ref->{SUBTYPE} : "---");
  $out .= ($ref->{ELEMENT_TYPE} eq "entity" ?
	   sprintf ("/%3.3s/%3.3s):", $ref->{LEVEL}, $ref->{CLASS}) :
	   "):        ")
    ." ID=$id";
  my $data;
  (my $tag, $data) = (($data=longest_string($ref, "name")) ? ("name", $data) :
		      (($data=longest_string($ref, "mention", "head")) ? ("head", $data) :
		       (($data=longest_string($ref, "mention", "extent")) ? ("extent", $data) : "")));
  $out .= ", $tag=\"$data\"" if $data;
  $out .= $text ? " -- $text\n" : "\n";
  return $out;
}	

#################################

sub limit_string {

  my ($string, $max_length) = @_;

  my $max = $max_length ? $max_length : $max_string_length_to_print;
  return length $string <= $max ? $string : substr ($string, 0, $max-3)."...";
}

#################################

sub print_event_mentions {

  my ($event, $ref_refs) = @_;

  while ((my $doc, my $doc_event) = each %{$event->{documents}}) {
    print "      -- in document $doc\n";
    foreach my $mention (@{$doc_event->{mentions}}) {
      printf "         mention ID=$mention->{ID}";
      printf ", LEVEL=$mention->{LEVEL}" if $mention->{LEVEL};
      foreach my $anchor (@{$mention->{anchors}}) {
	print ", anchor=\"$anchor->{text}\"";
      }
      print $mention->{extent}{text} ? ", extent=\"$mention->{extent}{text}\"\n" : "\n";
      foreach my $role (sort keys %{$mention->{arguments}}) {
	foreach my $id (sort keys %{$mention->{arguments}{$role}}) {
	  my $ref = $ref_refs->{$mention->{arguments}{$role}{$id}{ID}};
	  printf "%21s: ID=$id%s", $role, ($ref->{head} ? ", head=\"$ref->{head}{text}\"\n" :
					   ($ref->{extent} ? ", extent=\"$ref->{extent}{text}\"\n" : "\n"));
	}
      }
    }
  }
}

#################################

sub compute_releve_values {

  my ($refs, $tsts, $arg_scorer, $doc_scorer) = @_;

#compute self-values
  foreach my $element (values %$refs, values %$tsts) {
    while ((my $doc, my $doc_element) = each %{$element->{documents}}) {
      ($doc_element->{VALUE}) = &$doc_scorer ($element, undef, $doc);
      ($doc_element->{FA_VALUE}) = &$doc_scorer (undef, $element, $doc);
      $element->{VALUE} += $doc_element->{VALUE};
      $element->{FA_VALUE} = $doc_element->{FA_VALUE};
    }
  }

#select putative REF-TST element pairs for mapping
  my %putative_pairs;
  foreach my $doc (keys %eval_docs) {
    my %doc_refs;
    while ((my $ref_id, my $ref) = each %$refs) {
      $doc_refs{$ref_id} = 1 if defined $ref->{documents}{$doc};
    }
    next unless %doc_refs;
    while ((my $tst_id, my $tst) = each %$tsts) {
      next unless defined $tst->{documents}{$doc};
      foreach my $ref_id (keys %doc_refs) {
	$putative_pairs{$ref_id}{$tst_id} = $tst;
      }
    }
  }

#compute mapped element values
  while ((my $ref_id, my $putative_tsts) = each %putative_pairs) {
    my $ref = $refs->{$ref_id};
    while ((my $tst_id, my $tst) = each %$putative_tsts) {
      my $arg_map = &$arg_scorer ($ref, $tst);
      my $value;
      foreach my $doc (keys %{$ref->{documents}}) {
	(my $doc_value) = &$doc_scorer ($ref, $tst, $doc, $arg_map);
	next unless defined $doc_value;
	$mapped_document_values{$ref_id}{$tst_id}{$doc} = $doc_value;
	$value += $doc_value;
      }
      next unless defined $value;
      $mapped_values{$ref_id}{$tst_id} = $value;
      $argument_map{$ref_id}{$tst_id} = $arg_map;
    }
  }
}

#################################

sub map_releve_arguments {

  my ($ref, $tst) = @_;

  my $arg_map = $argument_map{$ref->{ID}}{$tst->{ID}};

#map arguments
  while ((my $tst_role, my $tst_ids) = each %$arg_map) {
    while ((my $tst_id, my $ref_map) = each %$tst_ids) {
      (my $ref_arg, my $tst_arg) = $ref->{ELEMENT_TYPE} eq "relation" ?
	($ref->{arguments}{$ref_map->{ROLE}}, $tst->{arguments}{$tst_role}) :
	($ref->{arguments}{$ref_map->{ROLE}}{$ref_map->{ID}}, $tst->{arguments}{$tst_role}{$tst_id});
      next unless $ref_arg and $tst_arg;
      $ref_arg->{MAP} = $tst_arg;
      $tst_arg->{MAP} = $ref_arg;
    }
  }

#map document level info
  foreach my $doc (keys %{$ref->{documents}}) {
    my $doc_ref = $ref->{documents}{$doc};
    my $doc_tst = $tst->{documents}{$doc};
    $doc_ref->{MAP} = $doc_tst;
    $doc_tst->{MAP} = $doc_ref;
    while ((my $tst_role, my $tst_ids) = each %$arg_map) {
      while ((my $tst_id, my $ref_map) = each %$tst_ids) {
	(my $ref_arg, my $tst_arg) = $ref->{ELEMENT_TYPE} eq "relation" ?
	  ($ref->{arguments}{$ref_map->{ROLE}}, $tst->{arguments}{$tst_role}) :
	  ($ref->{arguments}{$ref_map->{ROLE}}{$ref_map->{ID}}, $tst->{arguments}{$tst_role}{$tst_id});
	next unless $ref_arg and $tst_arg;
	$ref_arg->{MAP} = $tst_arg;
	$tst_arg->{MAP} = $ref_arg;
      }
    }
  }
}

#################################

sub event_document_value {

  my ($ref_event, $tst_event, $doc, $arg_map) = @_;

  my $fa_event = not $ref_event; #calculate FA score if ref is null
  $ref_event = $tst_event unless $ref_event;
  $tst_event = $ref_event unless $tst_event;
  my $ref_mentions = $ref_event->{documents}{$doc}{mentions};
  my $tst_mentions = $tst_event->{documents}{$doc}{mentions};
  $ref_mentions = () unless $ref_mentions;
  $tst_mentions = () unless $tst_mentions;

  if ($tst_event eq $ref_event) { #compute self-score
    my $arg_score = $ref_mentions ? $epsilon * @$ref_mentions : undef;
    my $arg_db = $fa_event ? $tst_database{refs} : $ref_database{refs};
    while ((my $role, my $ids) = each %{$ref_event->{arguments}}) {
      while ((my $id, my $arg) = each %$ids) {
	my $arg = $arg_db->{$ref_event->{arguments}{$role}{$id}{ID}};
	$arg_score += $arg->{documents}{$doc}{VALUE} if defined $arg->{documents}{$doc}{VALUE};
      }
    }
    my $event_value = $event_type_wgt{$ref_event->{TYPE}}*$event_modality_wgt{$ref_event->{MODALITY}};
    $event_value *= -$event_fa_wgt if $fa_event;
    return $event_value*$arg_score;
  }

#compute argument score
  (my $mentions_overlap, my $mentions_map) = optimum_event_mentions_mapping ($ref_mentions, $tst_mentions);
  return undef unless $mentions_overlap or $arg_map;
  my $arg_score = $mentions_overlap ? $mentions_overlap * $epsilon : undef;
  while ((my $tst_role, my $tst_ids) = each %{$tst_event->{arguments}}) {
    foreach my $tst_id (keys %$tst_ids) {
      my $ref_id = defined $arg_map->{$tst_role}{$tst_id} ? $arg_map->{$tst_role}{$tst_id}{ID} : "";
      my $map_score = ($ref_id and defined $mapped_document_values{$ref_id}{$tst_id}{$doc}) ?
	$mapped_document_values{$ref_id}{$tst_id}{$doc} : 0;
      my $ref_role = defined $arg_map->{$tst_role}{$tst_id} ? $arg_map->{$tst_role}{$tst_id}{ROLE} : ""; 
      $arg_score += (($ref_role and $tst_role eq $ref_role) ?
		     $map_score : $map_score * $event_argument_role_err_wgt)
	+ ($map_score - $tst_database{refs}{$tst_id}{documents}{$doc}{VALUE}) * $event_argument_fa_wgt;
    }
  }
  return undef unless defined $arg_score;

#compute event value
  my $event_value = min($event_type_wgt{$ref_event->{TYPE}}*$event_modality_wgt{$ref_event->{MODALITY}},
			$event_type_wgt{$tst_event->{TYPE}}*$event_modality_wgt{$tst_event->{MODALITY}});

#reduce value for errors in event attributes
  while ((my $attribute, my $weight) = each %event_err_wgt) {
    my $ref_attribute = $ref_event->{$attribute};
    my $tst_attribute = $tst_event->{$attribute};
    $event_value *= $weight if (($ref_attribute xor $tst_attribute) or
				   (defined $tst_attribute and $ref_attribute ne $tst_attribute));
  }
  return $event_value*$arg_score, $mentions_map;
}

#################################

sub optimum_event_mentions_mapping { #find the mapping that maximizes anchor overlap

  my ($ref_mentions, $tst_mentions) = @_;

  my (%mapping_costs, $total_overlap, $mentions_map);
  for (my $i=0; $i<@$ref_mentions; $i++) {
    next unless defined $ref_mentions->[$i]{anchors};
    my @ref_anchors = @{$ref_mentions->[$i]{anchors}};
    for (my $j=0; $j<@$tst_mentions; $j++) {
      next unless defined $tst_mentions->[$j]{anchors};
      my @tst_anchors = @{$tst_mentions->[$j]{anchors}};
      my $max_score = 0;
      foreach my $ref_anchor (@ref_anchors) {
	foreach my $tst_anchor (@tst_anchors) {
	  my $score = span_overlap($ref_anchor->{locator},
				   $tst_anchor->{locator});
	  $max_score = $score if $score > $max_score;
	}
      }
      $mapping_costs{$j}{$i} = -$max_score if $max_score > 0;
    }
  }
  return undef unless %mapping_costs;

  my ($map) = weighted_bipartite_graph_matching(\%mapping_costs) or die
    "\n\nFATAL ERROR:  Document level event mention mapping through BGM FAILED\n\n";
  for (my $j=0; $j<@$tst_mentions; $j++) {
    next unless defined (my $i = $map->{$j});
    $mentions_map->{$i} = $j;
    $total_overlap -= $mapping_costs{$j}{$i};
  }
  return $total_overlap, $mentions_map;
}

#################################

sub compute_event_argument_map {

  my ($ref_event, $tst_event) = @_;

#collect arguments
  my (@ref_args, @tst_args);
  while ((my $role, my $args) = each %{$ref_event->{arguments}}) {
    foreach my $id (keys %$args) {
      push @ref_args, {ID => $id, ROLE => $role};
    }
  }
  return undef unless @ref_args;
  while ((my $role, my $args) = each %{$tst_event->{arguments}}) {
    foreach my $id (keys %$args) {
      push @tst_args, {ID => $id, ROLE => $role};
    }
  }
  return undef unless @tst_args;

#compute argument mapping scores
  my %mapping_costs;
  for (my $i=0; $i<@ref_args; $i++) {
    my $ref_id = $ref_args[$i]->{ID};
    for (my $j=0; $j<@tst_args; $j++) {
      my $tst_id = $tst_args[$j]->{ID};
      next unless defined $mapped_values{$ref_id}{$tst_id};
      my $map_score = my $sys_score = 0;
      foreach my $doc (keys %{$tst_event->{documents}}) {
	$map_score += $mapped_document_values{$ref_id}{$tst_id}{$doc} if
	  defined $mapped_document_values{$ref_id}{$tst_id}{$doc};
	$sys_score += $tst_database{refs}{$tst_id}{documents}{$doc}{VALUE};
      }
      my $score = ($ref_args[$i]->{ROLE} eq $tst_args[$j]->{ROLE} ?
		   $map_score : $map_score * $event_argument_role_err_wgt)
	+ ($map_score - $sys_score) * $event_argument_fa_wgt;
      my $fa_score = -$sys_score * $event_argument_fa_wgt;
      $mapping_costs{$j}{$i} = $fa_score - $score;
    }
  }
  return undef unless %mapping_costs;
    
#find optimum mapping
  my ($map) = weighted_bipartite_graph_matching(\%mapping_costs) or die
    "\n\nFATAL ERROR:  Event argument mapping through BGM FAILED\n\n";

  my %arg_map;
  for (my $j=0; $j<@tst_args; $j++) {
    next unless defined (my $i = $map->{$j});
    $arg_map{$tst_args[$j]->{ROLE}}{$tst_args[$j]->{ID}} = {ROLE => $ref_args[$i]->{ROLE},
							    ID => $ref_args[$i]->{ID}};
  }
  return {%arg_map};
}

#################################

sub print_releve_mapping {

  my ($type, $print_mention_mapping, $attributes) = @_;

  my $refs = $ref_database{$type."s"};
  my $tsts = $tst_database{$type."s"};
  foreach my $ref_id (sort keys %$refs) {
    my $print_data = $opt_a;
    my $output = "--------\n";
    my $ref = $refs->{$ref_id};
    my $tst = $ref->{MAP};
    if ($tst) {
      my $tst_id = $tst->{ID};
      my $attribute_errors;
      foreach my $attribute (@$attributes) {
	next if $attribute eq "ID";
	$attribute_errors = ($ref->{$attribute} xor $tst->{$attribute} or
			     defined $tst->{$attribute} and $tst->{$attribute} ne $ref->{$attribute});
	last if $attribute_errors;
      }
      $print_data ||= $opt_e if $attribute_errors;
      $output .= ($attribute_errors ? ">>> " : "    ").
	"ref $type $ref->{ID} ".list_element_attributes($ref, $attributes);
      $output .= ($attribute_errors ? " -- ATTRIBUTE ERRORS\n" : "\n");
      $output .= ($attribute_errors ? ">>> " : "    ").
	"tst $type $tst->{ID} ".list_element_attributes($tst, $attributes);
      $output .= ($attribute_errors ? " -- ATTRIBUTE ERRORS\n" : "\n");
      $output .= sprintf ("      $type score:  %.5f out of %.5f\n",
			  $mapped_values{$ref_id}{$tst_id}, $ref->{VALUE});
    } else {
      $print_data ||= $opt_e;
      $output .= ">>> ref $type $ref->{ID} ".
	list_element_attributes($ref, $attributes)." -- NO MATCHING TST\n";
      $output .= sprintf "      $type score:  0.00000 out of %.5f\n", $ref->{VALUE};
    }
    $print_data = print_argument_mapping ($ref, $tst, $print_data, $output);
    next unless $print_data;
    foreach my $doc (sort keys %{$ref->{documents}}) {
      &$print_mention_mapping ($ref, $tst, $doc) if $eval_docs{$doc};
    }
  }

#print unmapped test elements
  return unless $opt_a or $opt_e;
  foreach my $tst_id (sort keys %$tsts) {
    my $tst = $tsts->{$tst_id};
    next if $tst->{MAP};
    print "--------\n";
    print ">>> tst $type $tst->{ID} ".
      list_element_attributes($tst, $attributes)." -- NO MATCHING REF\n";
    printf "      $type score:  %.5f out of 0.00000\n", $tst->{FA_VALUE};
    print_argument_mapping (undef, $tst);
    foreach my $doc (sort keys %{$tst->{documents}}) {
      &$print_mention_mapping (undef, $tst, $doc) if $eval_docs{$doc};
    }
  }
}

#################################

sub list_element_attributes {

  my ($event, $attributes) = @_;

  my $output;
  for my $attribute ("TYPE", "SUBTYPE") {
    $output .= ($output ? "/" : "(").($event->{$attribute} ? $event->{$attribute} : "---");
  }
  for my $attribute (@$attributes) {
    next if $attribute =~ /^(TYPE|SUBTYPE|ID)$/;
    $output .= "/".($event->{$attribute} ? $event->{$attribute} : "---");
  }
  return "$output)"
}

#################################

sub print_argument_mapping {  

  my ($ref, $tst, $print, $output) = @_;

  my %args;
  if ($ref and $ref->{arguments}) {
    if ($ref->{ELEMENT_TYPE} eq "relation") {
      foreach my $arg (values %{$ref->{arguments}}) {
	$args{$arg}{REF} = $arg;
      }
    } else {
      while ((my $role, my $ids) = each %{$ref->{arguments}}) {
	foreach my $arg (values %$ids) {
	  $args{$arg}{REF} = $arg;
	}
      }
    }
  }
  if ($tst and $tst->{arguments}) {
    if ($tst->{ELEMENT_TYPE} eq "relation") {
      my $order = "normal";
      foreach my $arg (values %{$tst->{arguments}}) {
	$args{$arg->{MAP}?$arg->{MAP}:$arg}{TST} = $arg;
	$order = "reversed" if $arg->{ROLE} =~ /^Arg-[12]$/ and $arg->{MAP} and $arg->{MAP}{ROLE} ne $arg->{ROLE};
      }
      if ($order eq "reversed") {
	foreach my $arg (values %{$tst->{arguments}}) {
	  my $role = ($arg->{ROLE} eq "Arg-1" ? "Arg-2" :
		      ($arg->{ROLE} eq "Arg-2" ? "Arg-1" : undef));
	  $args{$arg->{MAP}?$arg->{MAP}:$arg}{TST}{ROLE} = $role if $role;
	}
      }
    } else {
      while ((my $role, my $ids) = each %{$tst->{arguments}}) {
	foreach my $arg (values %$ids) {
	  $args{$arg->{MAP}?$arg->{MAP}:$arg}{TST} = $arg;
	}
      }
    }
  }
  my @args = sort {my $cmp = ($a->{REF}?$a->{REF}{ROLE}:$a->{TST}{ROLE}) cmp ($b->{REF}?$b->{REF}{ROLE}:$b->{TST}{ROLE});
		   return $cmp if $cmp;
		   ($a->{REF}?$a->{REF}{ID}:$a->{TST}{ID}) cmp ($b->{REF}?$b->{REF}{ID}:$b->{TST}{ID});} values %args;
  foreach my $arg (@args) {
    my ($ref_err, $tst_err);
    my $ref_arg = $arg->{REF};
    my $tst_arg = $arg->{TST};
    if ($ref_arg) {
      $ref_err = "NO CORRESPONDING TST ARGUMENT" unless $tst_arg;
      my $a_ref = $ref_database{refs}{$ref_arg->{ID}};
      $ref_err .= ($ref_err ? ", " : "")."REF ARGUMENT NOT MAPPED" if not $a_ref->{MAP};
      $ref_err .= ($ref_err ? ", " : "")."REF ARGUMENT MISMAPPED" if $a_ref->{MAP} and $tst_arg and
	$a_ref->{MAP}{ID} ne $tst_arg->{ID};
      $ref_err .= ($ref_err ? ", " : "")."ARGUMENT ROLE MISMATCH" if $a_ref->{MAP} and $tst_arg and $ref_arg->{ROLE} !~ /^Arg-[12]/ and
	$a_ref->{MAP}{ID} eq $tst_arg->{ID} and $ref_arg->{ROLE} ne $tst_arg->{ROLE};
      $output .= ($ref_err ? ">>>   ":"      ").mapped_argument_description ("ref", $ref_arg, $ref_err);
      $print ||= $ref_err;
    }
    if ($tst_arg) {
      $tst_err = "NO CORRESPONDING REF ARGUMENT" unless $ref_arg;
      my $a_ref = $tst_database{refs}{$tst_arg->{ID}};
      $tst_err .= ($tst_err ? ", " : "")."TST ARGUMENT NOT MAPPED" if not $a_ref->{MAP};
      $tst_err .= ($tst_err ? ", " : "")."TST ARGUMENT MISMAPPED" if $a_ref->{MAP} and $ref_arg and
	$a_ref->{MAP}{ID} ne $ref_arg->{ID};
      $tst_err .= ($tst_err ? ", " : "")."ARGUMENT ROLE MISMATCH" if $a_ref->{MAP} and $ref_arg and $tst_arg->{ROLE} !~ /^Arg-[12]/ and
	$a_ref->{MAP}{ID} eq $ref_arg->{ID} and $tst_arg->{ROLE} ne $ref_arg->{ROLE};
      $output .= ($tst_err ? ">>>   ":"      ").mapped_argument_description ("tst", $tst_arg, $tst_err)
	if $ref_err or $tst_err;
      $print ||= $tst_err;
    }
  }
  print $output if $print;
  return $print;
}

#################################

sub mapped_argument_description {

  my ($src, $arg, $text) = @_;

  my $id = $arg->{ID};
  my $ref = $src eq "ref" ? $ref_database{refs}{$id} : $tst_database{refs}{$id};
  my $out = sprintf ("%11.11s $src arg: ID=$id (%3.3s/", $arg->{ROLE}, $ref->{TYPE});
  $out .= $ref->{SUBTYPE} ? (substr $ref->{SUBTYPE}, 0, 7) : "---";
  $out .= $ref->{ELEMENT_TYPE} eq "entity" ? sprintf ("/%3.3s/%3.3s)", $ref->{LEVEL}, $ref->{CLASS}) : ")";
  my $data;
  (my $tag, $data) = (($data=longest_string($ref, "name")) ? ("name", $data) :
		      (($data=longest_string($ref, "mention", "head")) ? ("head", $data) :
		       (($data=longest_string($ref, "mention", "extent")) ? ("extent", $data) : "")));
  $out .= ", $tag=\"$data\"" if $data;
  $out .= $text ? " -- $text\n" : "\n";
  return $out;
}

#################################

sub print_event_mention_mapping {

  my ($ref_event, $tst_event, $doc) = @_;

  my $ref_id = $ref_event->{ID};
  my $tst_id = $tst_event->{ID};
  my $doc_ref = $ref_event ? $ref_event->{documents}{$doc} : undef;
  my $doc_tst = $tst_event ? $tst_event->{documents}{$doc} : undef;
  my $ref_value = $doc_ref ? $doc_ref->{VALUE} : 0;
  my $tst_value = ($doc_ref ? ($doc_tst ? (defined $mapped_document_values{$ref_id}{$tst_id}{$doc} ? 
					   $mapped_document_values{$ref_id}{$tst_id}{$doc}
					   : 0)
			       : 0)
		   : $doc_tst->{FA_VALUE});
  printf "- in document $doc:  score:  %.5f out of %.5f\n", $tst_value, $ref_value;
			      
  my @mentions;
  if ($ref_event) {
    foreach my $mention (@{$ref_event->{documents}{$doc}{mentions}}) {
      push @mentions, {DATA=>$mention->{anchors}[0], MENTION=>$mention, TYPE=>"REF"};
    }
  }
  if ($tst_event) {
    foreach my $mention (@{$tst_event->{documents}{$doc}{mentions}}) {
      push @mentions, {DATA=>$mention->{anchors}[0], MENTION=>$mention, TYPE=>"TST"};
    }
  }
  @mentions = sort compare_locators @mentions;
  while (my $mention = shift @mentions) {
    if ($mentions[0] and
	span_overlap($mention->{DATA}{locator}, $mentions[0]->{DATA}{locator}) > $min_overlap and
	$mention->{TYPE} ne $mentions[0]->{TYPE}) {
      $mention = $mentions[0] if $mentions[0]->{TYPE} eq "REF";
      $mention->{TYPE} = "BOTH";
      shift @mentions;
    }
    print $mention->{TYPE} eq "BOTH" ? "          " : ($mention->{TYPE} eq "REF" ? ">>>   ref " : ">>>   tst ");
    foreach my $anchor (@{$mention->{MENTION}{anchors}}) {
      print " anchor=\"$anchor->{text}\"";
    }
    print ", extent=\"$mention->{MENTION}{extent}{text}\"" if $mention->{MENTION}{extent}{text};
    print $mention->{TYPE} eq "BOTH" ? "\n" : " -- unmatched mention\n";
  }
}

#################################

sub get_timex2s { #extract document-level information for all timex2s in the document

  my ($data) = @_;
  my (@timex2s, %timex2_ids);
  my ($tag, $span, $modality, $class, $attribute, $mention);

  while (($tag, $span, $data) = extract_sgml_tag_and_span ("quantity", $data)) {
    next unless "TIMEX2" eq demand_attribute ("quantity", "TYPE", $tag, $quantity_attributes{TYPE});
    my %timex2;
    $input_element = demand_attribute ("TIMEX2", "ID", $tag);
    $input_element =~ s/^\s*|\s*$//g; #trim any white space from beginning/end of timex2 ID
    not defined $timex2_ids{$input_element} or die
      "\n\nFATAL INPUT ERROR:  multiple definitions of timex2 '$input_element'\n".
	"              (every timex2 ID must be unique)\n\n";
    $timex2_ids{$input_element} = 1;
    $timex2{ID} = $input_element;
    $timex2{TYPE} = "TIMEX2";
    $fatal_input_error_header =
      "\n\nFATAL INPUT ERROR for timex2 '$input_element' in document '$input_doc' in file '$input_file'\n";

    foreach $attribute (@timex2_attributes) {
      my $value = get_attribute ("TIMEX2", "TIMEX2_".$attribute, $tag);
      $timex2{$attribute} = $value if defined $value;
    }
    $timex2{mentions} = [get_timex2_mentions (\%timex2, $span)];

    #set default values
    $timex2{SET} = "NO" unless defined $timex2{SET};
    $timex2{NON_SPECIFIC} = "NO" unless defined $timex2{NON_SPECIFIC};
    push @timex2s, {%timex2};
  }
  return @timex2s;
}

#################################

sub get_timex2_mentions { #extract mention information for all mentions of a timex2 in a document

  my ($timex2, $data) = @_;

  my @mentions;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("quantity_mention", $data)) {
    my %mention;
    $mention{ID} = demand_attribute ("TIMEX2_mention", "ID", $tag);
    $mention{extent} = get_locator ("extent", $span);
    defined $mention{extent} or die $fatal_input_error_header.
      "    no mention extent found in data ($span)\n\n";
    push @mentions, {%mention};
  }
  @mentions > 0 or die $fatal_input_error_header.
    "    timex2 contains no mentions\n\n";
  return @mentions;
}

#################################

sub timex2_document_value {

  my ($ref_timex2, $tst_timex2, $doc) = @_;

  my $fa_timex2 = not $ref_timex2; #calculate FA score if ref is null
  $ref_timex2 = $tst_timex2 if not $ref_timex2;
  $tst_timex2 = $ref_timex2 if not $tst_timex2;
  my $ref_mentions = $ref_timex2->{documents}{$doc}{mentions};
  my $tst_mentions = $tst_timex2->{documents}{$doc}{mentions};

  if ($ref_timex2 eq $tst_timex2) { #compute self-score
    my $mentions_score = 0;
    foreach my $mention (@$ref_mentions) {
      $mention->{self_score} = quantity_mention_score ($mention, $mention);
      $mentions_score += $mention->{self_score} if $mention->{self_score};
    }
    $mentions_score += -$timex2_mention_fa_wgt if $fa_timex2;
    my $timex2_value = $timex2_detection_wgt;
    $timex2_value *= $timex2_fa_wgt if $fa_timex2;
    $timex2_value = max($timex2_value, $epsilon);
    return $timex2_value*$mentions_score;
  }

#find optimum mapping of ref mentions to tst mentions
  my (%mapping_costs, $mentions_score, $mentions_map, @fa_scores);
  for (my $j=0; $j<@$tst_mentions; $j++) {
    $fa_scores[$j] = -$tst_mentions->[$j]{self_score} * $timex2_mention_fa_wgt;
    for (my $i=0; $i<@$ref_mentions; $i++) {
      next unless defined (my $tst_scores = $ref_mentions->[$i]{tst_scores});
      next unless defined (my $tst_score = $tst_scores->{$tst_mentions->[$j]});
      $mapping_costs{$j}{$i} = $fa_scores[$j] - $tst_score;
    }
  }
  return undef unless %mapping_costs;
  my ($map) = weighted_bipartite_graph_matching(\%mapping_costs) or die
    "\n\nFATAL ERROR:  Document level timex2 mention mapping through BGM FAILED\n\n";
  for (my $j=0; $j<@$tst_mentions; $j++) {
    next unless defined (my $i = $map->{$j});
    $mentions_map->{$i} = $j;
    $mentions_score += $fa_scores[$j] - $mapping_costs{$j}{$i};
  }

#compute timex2 value
  my $timex2_value = $timex2_detection_wgt;
  while ((my $attribute, my $weight) = each %timex2_attribute_wgt) {
    my $ref_att = $ref_timex2->{$attribute};
    my $tst_att = $tst_timex2->{$attribute};
    $timex2_value += $weight * ($ref_att eq $tst_att ? 1 : $epsilon)
      if (defined $ref_att and defined $tst_att);
  }
  $timex2_value = max($timex2_value,$epsilon);
  return $timex2_value*$mentions_score, $mentions_map;
}
      
#################################

sub print_timex2s {

  my ($db) = @_;

  foreach my $id (sort keys %{$db->{timex2s}}) {
    my $timex2 = $db->{timex2s}{$id};
    printf "timex2 ID=$timex2->{ID}, VALUE=%.5f, TYPE=$timex2->{TYPE}", $timex2->{VALUE};
    foreach my $attribute (@timex2_attributes) {
      print ", $attribute=$timex2->{$attribute}" unless
	$attribute =~ /^(ID|TYPE)$/ or not defined $timex2->{$attribute};
    }
    print "\n";
    foreach my $doc (sort keys %{$timex2->{documents}}) {
      my $doc_info = $timex2->{documents}{$doc};
      print "    -- in document $doc\n";
      foreach my $mention (sort compare_locators @{$doc_info->{mentions}}) {
	printf "      mention extent=\"%s\"\n", defined $mention->{extent}{text} ? $mention->{extent}{text} : "???";
      }
    }
  }
}

#################################

sub print_timex2_mapping {

  my ($ref_db, $tst_db) = @_;
  my ($ref_timex2, $tst_timex2, $ref_id, $tst_id, $doc, $ref_occ, $tst_occ, $attribute, $output);

  foreach $ref_id (sort keys %{$ref_db->{timex2s}}) {
    my $print_data = $opt_a;
    $output = "--------\n";
    $ref_timex2 = $ref_db->{timex2s}{$ref_id};
    if ($tst_timex2 = $ref_timex2->{MAP}) {
      $tst_id = $tst_timex2->{ID};
      my $attribute_error;
      foreach $attribute (@timex2_attributes) {
	$attribute_error .= ($attribute_error ? "/" : "").$attribute if
	  $attribute ne "ID" and
	  ($ref_timex2->{$attribute} and
	   (not $tst_timex2->{$attribute} or
	    $ref_timex2->{$attribute} ne $tst_timex2->{$attribute}));
      }
      $print_data ||= $opt_e if $attribute_error;
      $output .= ($attribute_error ? ">>> " : "    ")."ref timex2 $ref_id";
      foreach $attribute (@timex2_attributes) {
	next if not defined $ref_timex2->{$attribute};
	$output .= ", $attribute=$ref_timex2->{$attribute}";
      }
      $output .= "\n";

      $output .= ($attribute_error ? ">>> " : "    ")."tst timex2 $tst_id";
      foreach $attribute (@timex2_attributes) {
	next if not defined $tst_timex2->{$attribute};
	$output .= ", $attribute=$tst_timex2->{$attribute}";
      }
      $output .= $attribute_error ? " -- ATTRIBUTE ERROR ($attribute_error)\n" : "\n";

      $output .= sprintf ("      timex2 score:  %.5f out of %.5f\n",
			  $mapped_values{$ref_id}{$tst_id}, $ref_timex2->{VALUE});
    } else {
      $print_data ||= $opt_e;
      $output .= ">>> ref timex2 $ref_id -- NO MATCHING TST TIMEX2\n";
      $output .= sprintf ("      timex2 score:  0.00000 out of %.5f\n", $ref_timex2->{VALUE});
    }
    foreach $doc (keys %{$ref_timex2->{documents}}) {
      next unless defined $eval_docs{$doc};
      $ref_occ = $ref_timex2->{documents}{$doc};
      print_timex2_mapping_details ($ref_occ, $ref_occ->{MAP}, $doc, $print_data, $output);
      $output = "";
    }
  }

  return unless $opt_a or $opt_e;
  foreach $tst_id (sort keys %{$tst_db->{timex2s}}) {
    $tst_timex2 = $tst_db->{timex2s}{$tst_id};
    next if $tst_timex2->{MAP};
    $output = "--------\n";
    $output .= ">>> tst timex2 $tst_id";
    foreach $attribute (@timex2_attributes) {
      next if not defined $tst_timex2->{$attribute};
      $output .= ", $attribute=$tst_timex2->{$attribute}";
    }
    $output .= " -- NO MATCHING REF TIMEX2\n";
    $output .= sprintf ("      timex2 score:  %.5f out of 0.00000\n", $tst_timex2->{FA_VALUE});
    foreach $doc (keys %{$tst_timex2->{documents}}) {
      next unless defined $eval_docs{$doc};
      $tst_occ = $tst_timex2->{documents}{$doc};
      print_timex2_mapping_details (undef, $tst_occ, $doc, $opt_a, $output);
      $output = "";
    }
  }
}

#################################

sub print_timex2_mapping_details {

  my ($ref_timex2, $tst_timex2, $doc, $print_data, $output) = @_;
  my ($type, $ref_mention, $tst_mention, $mention, @mentions);

  $output .= "- in document $doc:\n";
  if ($ref_timex2) {
    foreach $mention (@{$ref_timex2->{mentions}}) {
      push @mentions, {DATA=>$mention, TYPE=>"REF"};
    }
  }
  if ($tst_timex2) {
    foreach $mention (@{$tst_timex2->{mentions}}) {
      push @mentions, {DATA=>$mention, TYPE=>"TST"};
    }
  }
  if ($ref_timex2 and $tst_timex2) {
    foreach $mention (sort compare_locators @mentions) {
      $type = $mention->{TYPE};
      $mention = $mention->{DATA};
      next if $type eq "TST" and $mention->{MAP};
      if ($mention->{MAP}) {
	$ref_mention = $mention;
	$tst_mention = $mention->{MAP};
	my $extent_error = 
	  extent_mismatch ($ref_mention->{extent}{locator}, $tst_mention->{extent}{locator}) > $epsilon;
	$print_data ||= $opt_e if $extent_error;
	if (not $extent_error and
	    defined $ref_mention->{extent}{text} and
	    defined $tst_mention->{extent}{text} and
	    $ref_mention->{extent}{text} eq $tst_mention->{extent}{text}) {
	  $output .= "          mention=\"" . $ref_mention->{extent}{text} . "\"\n";
	} else {
	  $output .= $extent_error ? ">>>   " : "      ";
	  $output .= "ref mention=\"" . (defined $ref_mention->{extent}{text} ? $ref_mention->{extent}{text} : "???") . "\"";
	  $output .= "\n";
	  $output .= $extent_error ? ">>>   " : "      ";
	  $output .= "tst mention=\"" . (defined $tst_mention->{extent}{text} ? $tst_mention->{extent}{text} : "???") . "\"";
	  $output .= $extent_error ? " -- EXTENT ERROR\n" : "\n";
	}
      } else {
	$print_data ||= $opt_e;
	$output .= ">>>   ".(lc$type)." mention=\"" . (defined $mention->{extent}{text} ? $mention->{extent}{text} : "???") . "\"";
	$output .= " -- NO MATCHING %s MENTION\n",
      }
    }
  } else {
    $print_data ||= $opt_e;
    foreach $mention (sort compare_locators @mentions) {
      $type = $mention->{TYPE};
      $mention = $mention->{DATA};
      $output .= "      ".(lc$type)." mention=\"" . (defined $mention->{extent}{text} ? $mention->{extent}{text} : "???") . "\"";
      $output .= "\n";
    }
  }
  print $output if $print_data;
}

#################################

sub get_quantities { #extract document-level information for all quantities in the document

  my ($data) = @_;

  my (@quantities, %quantity_ids);
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("quantity", $data)) {
    my %quantity;
    $input_element = demand_attribute ("quantity", "ID", $tag);
    $input_element =~ s/^\s*|\s*$//g; #trim any white space from beginning/end of quantity ID
    not defined $quantity_ids{$input_element} or die $fatal_input_error_header.
      "    multiple definitions of quantity (every quantity ID must be unique)\n\n";
    $fatal_input_error_header =
      "\n\nFATAL INPUT ERROR for quantity '$input_element' in document '$input_doc' in file '$input_file'\n";
    $quantity{TYPE} = demand_attribute ("quantity", "TYPE", $tag, $quantity_attributes{TYPE});
    next if $quantity{TYPE} eq "TIMEX2";

    $quantity_ids{$input_element} = 1;
    $quantity{ID} = $input_element;
    $quantity{SUBTYPE} = demand_attribute ("quantity", "SUBTYPE", $tag, $quantity_attributes{TYPE}{$quantity{TYPE}});
    $quantity{SUBTYPE} = "" unless defined $quantity{SUBTYPE};
    $quantity{mentions} = get_quantity_mentions (\%quantity, $span);
    push @quantities, {%quantity};
  }
  return @quantities;
}

#################################

sub get_quantity_mentions { #extract mention information for all mentions of a quantity in a document

  my ($quantity, $data) = @_;

  my @mentions;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("quantity_mention", $data)) {
    my %mention;
    $mention{ID} = demand_attribute ("quantity_mention", "ID", $tag);
    $mention{extent} = get_locator ("extent", $span);
    defined $mention{extent} or die $fatal_input_error_header.
      "    no mention extent found in data ($span)\n\n";
    push @mentions, {%mention};
  }
  @mentions > 0 or die $fatal_input_error_header.
    "    quantity contains no mentions\n\n";
  return [@mentions];
}

#################################

sub quantity_document_value {
  
  my ($ref_quantity, $tst_quantity, $doc) = @_;

  my $fa_quantity = not $ref_quantity; #calculate FA score if ref is null
  $ref_quantity = $tst_quantity if not $ref_quantity;
  $tst_quantity = $ref_quantity if not $tst_quantity;
  my $ref_mentions = $ref_quantity->{documents}{$doc}{mentions};
  my $tst_mentions = $tst_quantity->{documents}{$doc}{mentions};

  if ($ref_quantity eq $tst_quantity) { #compute self-score
    my $mentions_score = 0;
    foreach my $mention (@$ref_mentions) {
      $mention->{self_score} = quantity_mention_score ($mention, $mention);
      $mentions_score += $mention->{self_score} if $mention->{self_score};
    }
    $mentions_score *= -$quantity_mention_fa_wgt if $fa_quantity;
    my $quantity_value = $quantity_type_wgt{$ref_quantity->{TYPE}};
    $quantity_value *= $quantity_fa_wgt if $fa_quantity;
    $quantity_value = max($quantity_value, $epsilon);
    return $quantity_value*$mentions_score;
  }

#find optimum mapping of ref mentions to tst mentions
  my (%mapping_costs, $mentions_score, $mentions_map, @fa_scores);
  for (my $j=0; $j<@$tst_mentions; $j++) {
    $fa_scores[$j] = -$tst_mentions->[$j]{self_score} * $quantity_mention_fa_wgt;
    for (my $i=0; $i<@$ref_mentions; $i++) {
      next unless defined (my $tst_scores = $ref_mentions->[$i]{tst_scores});
      next unless defined (my $tst_score = $tst_scores->{$tst_mentions->[$j]});
      $mapping_costs{$j}{$i} = $fa_scores[$j] - $tst_score;
    }
  }
  return undef unless %mapping_costs;
  my ($map) = weighted_bipartite_graph_matching(\%mapping_costs) or die
    "\n\nFATAL ERROR:  Document level quantity mention mapping through BGM FAILED\n\n";
  for (my $j=0; $j<@$tst_mentions; $j++) {
    next unless defined (my $i = $map->{$j});
    $mentions_map->{$i} = $j;
    $mentions_score += $fa_scores[$j] - $mapping_costs{$j}{$i};
  }
  return undef unless defined $mentions_score;

#compute quantity value
  my $quantity_value = min($quantity_type_wgt{$ref_quantity->{TYPE}},
			   $quantity_type_wgt{$tst_quantity->{TYPE}});
  $quantity_value = max($quantity_value,$epsilon);

#reduce value for errors in quantity attributes
  while ((my $attribute, my $weight) = each %quantity_err_wgt) {
    my $ref_attribute = $ref_quantity->{$attribute};
    my $tst_attribute = $tst_quantity->{$attribute};
    $quantity_value *= $weight if (($ref_attribute xor $tst_attribute) or
				   (defined $tst_attribute and $ref_attribute ne $tst_attribute));
  }
  return $quantity_value*$mentions_score, $mentions_map;
}

#################################

sub quantity_mention_score {

  #N.B.  The mention mapping score must be undef if tst doesn't match ref.

  my ($ref_mention, $tst_mention) = @_;

  if ($ref_mention->{head} and $tst_mention->{head}) {
    return (span_overlap ($ref_mention->{head}{locator},
			  $tst_mention->{head}{locator}) < $min_overlap) ? undef : 1;
  } elsif ($ref_mention->{extent} and $tst_mention->{extent}) {
    return (span_overlap ($ref_mention->{extent}{locator},
			  $tst_mention->{extent}{locator}, \&min) < $min_overlap) ? undef : 1;
  }
  return undef;
}
      
#################################

sub print_quantities {

  my ($db) = @_;

  foreach my $id (sort keys %{$db->{quantities}}) {
    my $quantity = $db->{quantities}{$id};
    printf "quantity ID=$quantity->{ID}, VALUE=%.5f, TYPE=$quantity->{TYPE}", $quantity->{VALUE};
    foreach my $attribute (@quantity_attributes) {
      print ", $attribute=$quantity->{$attribute}" unless
	$attribute =~ /^(ID|TYPE)$/ or not $quantity->{$attribute};
    }
    print "\n";
    foreach my $doc (sort keys %{$quantity->{documents}}) {
      my $doc_info = $quantity->{documents}{$doc};
      print "    -- in document $doc\n";
      foreach my $mention (sort compare_locators @{$doc_info->{mentions}}) {
	printf "      mention extent=\"%s\"\n", $mention->{extent}{text} ? $mention->{extent}{text} : "???";
      }
    }
  }
}

#################################

sub print_quantity_mapping {

  my ($ref_db, $tst_db) = @_;
  my ($ref_quantity, $tst_quantity, $ref_id, $tst_id, $doc, $ref_occ, $tst_occ, $attribute, $output);

  foreach $ref_id (sort keys %{$ref_db->{quantities}}) {
    my $print_data = $opt_a;
    $output = "--------\n";
    $ref_quantity = $ref_db->{quantities}{$ref_id};
    if ($tst_quantity = $ref_quantity->{MAP}) {
      $tst_id = $tst_quantity->{ID};
      my $attribute_error;
      foreach $attribute (@quantity_attributes) {
	$attribute_error .= ($attribute_error ? "/" : "").$attribute if
	  $attribute ne "ID" and
	  ($ref_quantity->{$attribute} and
	   (not $tst_quantity->{$attribute} or
	    $ref_quantity->{$attribute} ne $tst_quantity->{$attribute}));
      }
      $print_data ||= $opt_e if $attribute_error;
      $output .= ($attribute_error ? ">>> " : "    ")."ref quantity $ref_id";
      foreach $attribute (@quantity_attributes) {
	next if not $ref_quantity->{$attribute};
	$output .= ", $attribute=$ref_quantity->{$attribute}";
      }
      $output .= "\n";

      $output .= ($attribute_error ? ">>> " : "    ")."tst quantity $tst_id";
      foreach $attribute (@quantity_attributes) {
	next if not $tst_quantity->{$attribute};
	$output .= ", $attribute=$tst_quantity->{$attribute}";
      }
      $output .= $attribute_error ? " -- ATTRIBUTE ERROR ($attribute_error)\n" : "\n";

      $output .= sprintf ("      quantity score:  %.5f out of %.5f\n",
			  $mapped_values{$ref_id}{$tst_id}, $ref_quantity->{VALUE});
    } else {
      $print_data ||= $opt_e;
      $output .= ">>> ref quantity $ref_id -- NO MATCHING TST QUANTITY\n";
      $output .= sprintf ("      quantity score:  0.00000 out of %.5f\n", $ref_quantity->{VALUE});
    }
    foreach $doc (keys %{$ref_quantity->{documents}}) {
      next unless defined $eval_docs{$doc};
      $ref_occ = $ref_quantity->{documents}{$doc};
      print_quantity_mapping_details ($ref_occ, $ref_occ->{MAP}, $doc, $print_data, $output);
      $output = "";
    }
  }

  return unless $opt_a or $opt_e;
  foreach $tst_id (sort keys %{$tst_db->{quantities}}) {
    $tst_quantity = $tst_db->{quantities}{$tst_id};
    next if $tst_quantity->{MAP};
    $output = "--------\n";
    $output .= ">>> tst quantity $tst_id";
    foreach $attribute (@quantity_attributes) {
      next if not $tst_quantity->{$attribute};
      $output .= ", $attribute=$tst_quantity->{$attribute}";
    }
    $output .= " -- NO MATCHING REF QUANTITY\n";
    $output .= sprintf ("      quantity score:  %.5f out of 0.00000\n", $tst_quantity->{FA_VALUE});
    foreach $doc (sort keys %{$tst_quantity->{documents}}) {
      next unless defined $eval_docs{$doc};
      $tst_occ = $tst_quantity->{documents}{$doc};
      print_quantity_mapping_details (undef, $tst_occ, $doc, $opt_a, $output);
      $output = "";
    }
  }
}

#################################

sub print_quantity_mapping_details {

  my ($ref_quantity, $tst_quantity, $doc, $print_data, $output) = @_;
  my ($type, $ref_mention, $tst_mention, $mention, @mentions);

  $output .= "- in document $doc:\n";
  if ($ref_quantity) {
    foreach $mention (@{$ref_quantity->{mentions}}) {
      push @mentions, {DATA=>$mention, TYPE=>"REF"};
    }
  }
  if ($tst_quantity) {
    foreach $mention (@{$tst_quantity->{mentions}}) {
      push @mentions, {DATA=>$mention, TYPE=>"TST"};
    }
  }
  if ($ref_quantity and $tst_quantity) {
    foreach $mention (sort compare_locators @mentions) {
      $type = $mention->{TYPE};
      $mention = $mention->{DATA};
      next if $type eq "TST" and $mention->{MAP};
      if ($mention->{MAP}) {
	$ref_mention = $mention;
	$tst_mention = $mention->{MAP};
	my $extent_error = 
	  extent_mismatch ($ref_mention->{extent}{locator}, $tst_mention->{extent}{locator}) > $epsilon;
	$print_data ||= $opt_e if $extent_error;
	if (not $extent_error and
	    $ref_mention->{extent}{text} and
	    $tst_mention->{extent}{text} and
	    $ref_mention->{extent}{text} eq $tst_mention->{extent}{text}) {
	  $output .= "          mention=\"" . $ref_mention->{extent}{text} . "\"\n";
	} else {
	  $output .= $extent_error ? ">>>   " : "      ";
	  $output .= "ref mention=\"" . ($ref_mention->{extent}{text} ? $ref_mention->{extent}{text} : "???") . "\"";
	  $output .= "\n";
	  $output .= $extent_error ? ">>>   " : "      ";
	  $output .= "tst mention=\"" . ($tst_mention->{extent}{text} ? $tst_mention->{extent}{text} : "???") . "\"";
	  $output .= $extent_error ? " -- EXTENT ERROR\n" : "\n";
	}
      } else {
	$print_data ||= $opt_e;
	$output .= ">>>   ".(lc$type)." mention=\"" . ($mention->{extent}{text} ? $mention->{extent}{text} : "???") . "\"";
	$output .= " -- NO MATCHING %s MENTION\n",
      }
    }
  } else {
    $print_data ||= $opt_e;
    foreach $mention (sort compare_locators @mentions) {
      $type = $mention->{TYPE};
      $mention = $mention->{DATA};
      $output .= "      ".(lc$type)." mention=\"" . ($mention->{extent}{text} ? $mention->{extent}{text} : "???") . "\"";
      $output .= "\n";
    }
  }
  print $output if $print_data;
}

#################################

sub get_entities { #extract document-level information for all entities in the document

  my ($data) = @_;

  my (@entities, %entity_ids);
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("entity", $data)) {
    my %entity;
    #get entity ID
    $input_element = demand_attribute ("entity", "ID", $tag);
    $input_element =~ s/^\s*|\s*$//g; #trim any white space from beginning/end of entity ID
    not defined $entity_ids{$input_element} or die $fatal_input_error_header.
      "    multiple entity definitions (entities may be defined only once per document)\n\n";
    $entity_ids{$input_element} = 1;
    $entity{ID} = $input_element;
    $fatal_input_error_header =
      "\n\nFATAL INPUT ERROR for entity '$input_element' in document '$input_doc' in file '$input_file'\n";

    $entity{TYPE} = demand_attribute ("entity", "TYPE", $tag, $entity{TYPE});
    $entity{SUBTYPE} = demand_attribute ("entity", "SUBTYPE", $tag, $entity_attributes{TYPE}{$entity{TYPE}});
    $entity{SUBTYPE} = "" unless defined $entity{SUBTYPE};
    $entity{CLASS} = demand_attribute ("entity", "CLASS", $tag, \%entity_class_wgt);
    $entity{mentions} = get_entity_mentions (\%entity, $span);
    $entity{names} = get_entity_names ($span);
    $entity{LEVEL} = entity_level (\%entity);
    $entity{external_links} = get_external_links ($span);
    push @entities, {%entity};
  }
  return @entities;
}

#################################

sub get_entity_mentions { #extract mention information for all mentions of an entity in a document

 my ($entity, $data) = @_;

  my @mentions;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("entity_mention", $data)) {
    my %mention;
    $mention{ID} = demand_attribute ("entity_mention", "ID", $tag);
    $mention{TYPE} = demand_attribute ("entity_mention", "TYPE", $tag, \%entity_mention_type_wgt);
    my $role = get_attribute ("entity_mention", "ROLE", $tag, \%entity_type_wgt);
    $mention{ROLE} = defined $role ? $role : $entity->{TYPE};
    my $metonymy = get_attribute ("entity_mention", "METONYMY_MENTION", $tag, {TRUE=>1, FALSE=>1});
    $mention{STYLE} = ($metonymy and $metonymy eq "TRUE") ? "METONYMIC" : "LITERAL";
    $mention{LDCTYPE} = extract_sgml_tag_attribute ("LDCTYPE", $tag);
    $mention{LDCATR} = extract_sgml_tag_attribute ("LDCATR", $tag);
    $mention{extent} = get_locator ("extent", $span) or die $fatal_input_error_header.
	"    no mention extent found in data ($span)\n\n";
    $mention{head} = get_locator ("head", $span);
    push @mentions, {%mention};
  }
  @mentions > 0 or die $fatal_input_error_header.
      "    entity contains no mentions\n\n";
  return [@mentions];
}

#################################

sub entity_level {

  my ($entity) = @_;
  my $level;
  my $weight = 0;
  foreach my $mention (@{$entity->{mentions}}) {
    next if $entity_mention_type_wgt{$mention->{TYPE}} <= $weight;
    $weight = $entity_mention_type_wgt{$mention->{TYPE}};
    $level = $mention->{TYPE};
  }
  return $level;
}

#################################

sub longest_string {

  my ($element, $kind, $type) = @_;

# $kind is either "mention" or "name"
# $type is either "head" or "extent" (for $kind eq "mention") or undef (for $kind eq "name")

  my $longest_string="";

  my $kinds = $kind."s";
  if (defined $element->{$kinds}) {
    foreach my $kind (@{$element->{$kinds}}) {
      my $text = ($type and $kind->{$type}) ? $kind->{$type}{text} : $kind->{text};
      $longest_string = $text if defined $text and length($text) > length($longest_string);
    }
  }

  if (defined $element->{documents}) {
    foreach my $doc (keys %{$element->{documents}}) {
      if (defined $element->{documents}{$doc}{$kinds}) {
	foreach my $kind (@{$element->{documents}{$doc}{$kinds}}) {
	  my $text = ($type and $kind->{$type}) ? $kind->{$type}{text} : $kind->{text};
	  $longest_string = $text if defined $text and length($text) > length($longest_string);
	}
      }
    }
  }
  
  return $longest_string;
}

#################################
    
sub get_entity_names { #extract name information for all names of an entity in a document

  my ($data) = @_;
    
  my @names;
  ($data) = extract_sgml_span ("entity_attributes", $data);
  while (my $name=get_locator("name",$data)) {
    (my $tag, my $span, $data) = extract_sgml_tag_and_span ("name", $data);
    my $text = get_attribute ("name", "NAME", $tag);
    $name->{text} = limit_string $text if $text;
    push @names, $name;
  }
  return [@names];
}

#################################
    
sub get_external_links {

 my ($data) = @_;

  my @links;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("external_link", $data)) {
    my %link;
    $link{RESOURCE} = demand_attribute ("external_link", "RESOURCE", $tag);
    $link{ID} = demand_attribute ("external_link", "EID", $tag);
    push @links, {%link};
  }
  return @links ? [@links] : undef;
}

#################################
    
sub promote_external_links {

  my ($db_entity, $entity) = @_;

  return unless defined $entity->{external_links};
  my %new_links;
  foreach my $link (@{$entity->{external_links}}) {
    not defined $new_links{$link->{RESOURCE}} or die
      "\n\nFATAL INPUT ERROR:  multiple external ID's ('$link->{ID}'/'$new_links{$link->{RESOURCE}}')".
      "given for resource '$link->{RESOURCE}' for entity '$entity->{ID}'\n\n";
    $new_links{$link->{RESOURCE}} = $link->{ID};
  }

  if (defined $db_entity->{external_links}) {
    foreach my $link (@{$db_entity->{external_links}}) {
      next unless defined $new_links{$link->{RESOURCE}};
      $link->{ID} eq $new_links{$link->{RESOURCE}} or die
	"\n\nFATAL INPUT ERROR:  conflicting external ID's ('$link->{ID}'/'$new_links{$link->{RESOURCE}}')".
	"given for resource '$link->{RESOURCE}' for entity '$entity->{ID}'\n\n";
      delete $new_links{$link->{RESOURCE}};
    }
  }

  while ((my $resource, my $id) = each %new_links) {
    push @{$db_entity->{external_links}}, {RESOURCE => $resource, ID => $id};
  }
}

#################################
    
sub get_locator {

  my ($name, $data) = @_;

  my ($span, %info, $text);

  return undef unless ($span) = extract_sgml_span ($name, $data);

  if ($data_type eq "text") {
    ($info{locator}) = get_text_locator ($span);
    $text = $info{locator}{text};
  } elsif ($data_type eq "audio") {
    ($info{locator}) = get_audio_locator ($span);
    $text = $info{locator}{text};
  } elsif ($data_type eq "image") {
    ($info{locator}) = [get_image_locator ($span)];
    $text = "";
    foreach my $bbox (@{$info{locator}}) {
      $text .= $bbox->{locator}{text};
    }
  } else {
    die $fatal_input_error_header.
      "    No locator read routine for '$data_type' for $name locator.  Data: '$data'\n\n";
  }
  $text =~ s/-\n//sg;
  $text =~ s/\n/ /sg;
  $text =~ s/\s+/ /sg;
  $info{text} = limit_string $text;
  $info{locator}{data_type} = $data_type;
  return {%info};
}

#################################
    
sub get_text_locator {

  my ($data) = @_;
  my (%locator, $tag, $span);

  ($tag, $span) = extract_sgml_tag_and_span ("charseq", $data) or die $fatal_input_error_header.
    "    text mention contains no position info (no 'charseq' tag): '$data'\n\n";

  ($locator{start}) = extract_sgml_tag_attribute ("START", $tag) or die $fatal_input_error_header.
    "    No 'START' attribute found in data '$data'\n\n";
  ($locator{end}) = extract_sgml_tag_attribute ("END", $tag) or die $fatal_input_error_header.
    "    No 'END' attribute found in data '$data'\n\n";
  $locator{end}-$locator{start} >= 0 or die $fatal_input_error_header.
    "    Negative text span in data '$data'\n\n";
  $locator{start} >= 0 or die $fatal_input_error_header.
    "    Negative character index in data '$data'\n\n";
  ($locator{text}) = $span;
  return {%locator};
}

#################################
    
sub get_audio_locator {

  my ($data) = @_;
  my (%locator, $tag, $span);

  ($tag, $span) = extract_sgml_tag_and_span ("timespan", $data) or die $fatal_input_error_header.
    "    audio mention contains no timing info (no 'times' tag): '$data'\n\n";

  ($locator{tstart}) = extract_sgml_tag_attribute ("START", $tag) or die $fatal_input_error_header.
    "    No 'START' tag found in data '$data'\n\n";
  ($locator{tdur}) = extract_sgml_tag_attribute ("END", $tag) - $locator{tstart} or die $fatal_input_error_header.
    "    No 'END' tag found in data '$data'\n\n";
  $locator{tdur} >= 0 or die $fatal_input_error_header.
    "    Negative time duration in data '$data'\n\n";
  $locator{tstart} >= 0 or die $fatal_input_error_header.
    "    Negative start time in data '$data'\n\n";
  ($locator{text}) = $span;
  return {%locator};
}

#################################
    
sub get_image_locator {

  my ($data) = @_;
  my (%box, @boxlist);
  my ($tag, $span);
    
  ($data) = extract_sgml_span ("bblist", $data);

  my $nboxes = 0;
  while (($tag, $span, $data) = extract_sgml_tag_and_span ("pixelboundingbox", $data)) {
    $box{page} = extract_sgml_tag_attribute ("Signal", $tag) or die $fatal_input_error_header.
      "    No 'Signal' attribute found in tag '$tag'\n\n";
    $box{x_start} = extract_sgml_tag_attribute ("x1", $tag) or die $fatal_input_error_header.
      "    No 'x1' tag found in data '$tag'\n\n";
    $box{y_start} = extract_sgml_tag_attribute ("y1", $tag) or die $fatal_input_error_header.
      "    No 'y1' tag found in data '$tag'\n\n";
    $box{width} = extract_sgml_tag_attribute ("x2", $tag) or die $fatal_input_error_header.
      "    No 'x2' tag found in data '$tag'\n\n";
    $box{height} = extract_sgml_tag_attribute ("y2", $tag) or die $fatal_input_error_header.
      "    No 'y2' tag found in data '$tag'\n\n";

    $box{width} >= 0 or die $fatal_input_error_header.
      "    Negative bounding box width in data '$tag'\n\n";
    $box{height} >= 0 or die $fatal_input_error_header.
      "    Negative bounding box height in data '$tag'\n\n";
    $box{x_start} >= 0 or die $fatal_input_error_header.
      "    Negative 'x1' in data '$tag'\n\n";
    $box{y_start} >= 0 or die $fatal_input_error_header.
      "    Negative 'y1' in data '$tag'\n\n";
    $box{text} = $span;

    push @boxlist, {%box};
    $nboxes++;
  }
  $nboxes > 0 or die $fatal_input_error_header.
    "    image mention contains no boxes (no 'pixelboundingbox' tag): '$data'\n\n";
  return @boxlist;
}

#################################

sub compare_locators {

  my $ax = $a;
  my $bx = $b;

  ($ax, $bx) = ($ax->{DATA}, $bx->{DATA}) if defined $ax->{DATA} and defined $bx->{DATA};

  $ax = defined $ax->{head}{locator} ? $ax->{head}{locator} :
    defined $ax->{extent}{locator} ? $ax->{extent}{locator} :
    defined $ax->{locator} ? $ax->{locator} : die
    "\n\nFATAL ERROR in input to compare_locators\n\n";

  $bx = defined $bx->{head}{locator} ? $bx->{head}{locator} :
    defined $bx->{extent}{locator} ? $bx->{extent}{locator} :
    defined $bx->{locator} ? $bx->{locator} : die
    "\n\nFATAL ERROR in input to compare_locators\n\n";

  if ($ax->{data_type} eq "text" and $bx->{data_type} eq "text") {
    return $ax->{start} <=> $bx->{start};
  } elsif ($ax->{data_type} eq "audio" and $bx->{data_type} eq "audio") {
    return $ax->{tstart} <=> $bx->{tstart};
  } elsif ($ax->{data_type} eq "image" and $bx->{data_type} eq "image") {
    my $ax_box = $ax->{bblist}[0];
    my $bx_box = $bx->{bblist}[0];
    my $cmp = $ax_box->{page} <=> $bx_box->{page};
    return $cmp if $cmp;
    $cmp = $ax_box->{y_start} <=> $bx_box->{y_start};
    return $cmp if $cmp;
    $cmp = $ax_box->{x_start} <=> $bx_box->{x_start};
    return $cmp;
  } else {
    die "\n\nFATAL ERROR in compare_locators\n\n";
  }
}

#################################

sub get_relations { #extract document-level information for all relations in the document

  my ($data) = @_;
  my (@relations, %relation_ids);
  my ($tag, $span, $subtype, $class, $attribute, $mention);

  while (($tag, $span, $data) = extract_sgml_tag_and_span ("relation", $data)) {
    $input_element = demand_attribute ("relation", "ID", $tag) or die
    $input_element =~ s/^\s*|\s*$//g; #trim any white space from beginning/end of relation ID
    not defined $relation_ids{$input_element} or die
      "\n\nFATAL INPUT ERROR:  multiple definitions of relation '$input_element'\n".
	"    (every relation ID must be unique)\n\n";
    $relation_ids{$input_element} = 1;
    my $relation = {ID => $input_element};
    $fatal_input_error_header =
      "\n\nFATAL INPUT ERROR for relation '$input_element' in document '$input_doc' in file '$input_file'\n";

    $relation->{TYPE} = demand_attribute ("relation", "TYPE", $tag, $relation_attributes{TYPE});
    $relation->{SUBTYPE} = demand_attribute ("relation", "SUBTYPE", $tag, $relation_attributes{TYPE}{$relation->{TYPE}});
    $relation->{arguments} = get_relation_arguments ("relation_argument", $span);
    $relation->{mentions} = [get_relation_mentions ($span)];
    push @relations, $relation;
  }
  return @relations;
}

#################################

sub get_relation_arguments {

  my ($arg_name, $data) = @_;

  my %arguments;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ($arg_name, $data)) {
    my %arg;
    $arg{ID} = demand_attribute ($arg_name, "REFID", $tag);
    $arg{ROLE} = demand_attribute ($arg_name, "ROLE", $tag, \%relation_argument_roles);
    not defined $arguments{$arg{ROLE}} or die $fatal_input_error_header.
      "    This relation has multiple definitions of the $arg{ROLE} argument\n\n";
    $arguments{$arg{ROLE}} = {%arg};
  }
  foreach my $role ("Arg-1", "Arg-2") {
    defined $arguments{$role} or die $fatal_input_error_header.
      "    This relation does not have an \"$role\" argument, which is required\n\n";
  }
  return {%arguments};
}

#################################

sub get_relation_mentions {

  my ($data) = @_;
    
  my @mentions;
  while ((my $tag, my $span, $data) = extract_sgml_tag_and_span ("relation_mention", $data)) {
    my %mention;
    $mention{ID} = demand_attribute ("relation_mention", "ID", $tag);
    $mention{arguments} = get_relation_arguments ("relation_mention_argument", $span);
    $mention{extent} = get_locator ("extent", $span);
    push @mentions, {%mention};
  }
  return @mentions;
}

#################################

sub extract_sgml_span {
    
  my ($name, $data) = @_;
    
  return () unless defined $name and defined $data and
    $data =~ /<$name\s*(((\s[^>]*?[^\/])?>(.*?)<\/$name\s*>)|((\s[^>]*?)?\/>))/si;
  return ($4 or $5 or $6) ? ($4, $5, $6) : ($9, undef, $10);
}

#################################

sub extract_sgml_tag_and_span {
    
  my ($name, $data) = @_;
    
  return () unless defined $name and defined $data and
    $data =~ /<$name\s*(((\s([^>]*?[^\/]))?>(.*?)<\/$name\s*>(.*))|((\s([^>]*?))?\/>(.*)))/si;
  return ($4 or $5 or $6) ? ($4, $5, $6) : ($9, undef, $10);
}

#################################

sub extract_sgml_tag_attribute {

  my ($name, $data) = @_;

  return () unless defined $name and defined $data and
    $data =~ /\s*$name\s*=\s*\"\s*([^\"]*?)\s*\"/si;
  return $1;
}

#################################

sub extract_sgml_tag_attributes {

  my ($data) = @_;
  my %attributes;

  return () unless defined defined $data;
  $attributes{uc$1} = $2 while $data =~ s/\s*([^\s]+)\s*=\s*\"\s*([^\"]*?)\s*\"//si;
  return %attributes ? {%attributes} : ();
}

#################################

sub date_time_stamp {

  my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime();
  my @months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);
  my $time = sprintf "%2.2d:%2.2d:%2.2d", $hour, $min, $sec;
  my $date = sprintf "%4.4s %3.3s %s", 1900+$year, $months[$mon], $mday;
  return ($date, $time);
}

#################################

sub print_documents {

  my ($type, $documents) = @_;
  my ($doc_id, $doc);

  print "\n======== $type documents ========\n\n";
  foreach $doc_id (sort keys %$documents) {
    $doc = $documents->{$doc_id};
    print "doc ID=$doc_id, ";
    print "TYPE=$doc->{TYPE}, ";
    print "FILE=$doc->{FILE}\n";
  }
}

#################################

sub id_plus_external_ids {

  my ($element) = @_;

  return $element->{ID} unless defined $element->{external_links};

  my $external_ids;
  foreach my $link (sort {$a->{ID} cmp $b->{ID};} @{$element->{external_links}}) {
    $external_ids .= ($external_ids ? ", " : "")."$link->{ID} in $link->{RESOURCE}";
  }
  return "$element->{ID} ($external_ids)";
}

#################################

sub print_entities {

  my ($db) = @_;
  my ($entity_id);

  foreach $entity_id (sort keys %{$db->{entities}}) {
    my $entity = $db->{entities}{$entity_id};
    printf "entity ID=%s, VALUE=%.5f, TYPE=%s, SUBTYPE=%s, LEVEL=%s, CLASS=%s", id_plus_external_ids($entity),
    $entity->{VALUE}, $entity->{TYPE}, $entity->{SUBTYPE} ? $entity->{SUBTYPE} : "<none>",
    $entity->{LEVEL}, $entity->{CLASS};
    foreach my $attribute (@entity_attributes) {
      print ", $attribute=$entity->{$attribute}" unless
	$attribute =~ /^(ID|TYPE|SUBTYPE|LEVEL|CLASS)$/ or not defined $entity->{$attribute};
    }
    print "\n";
    foreach my $name (@{$entity->{names}}) {
      print "  name=\"$name\"\n";
    }
    foreach my $title (@{$entity->{titles}}) {
      print "  title=\"$title\"\n";
    }
    print_entity_mentions ($entity);
  }
}

#################################

sub print_entity_mentions {

  my ($entity) = @_;

  foreach my $doc (sort keys %{$entity->{documents}}) {
    my $doc_entity = $entity->{documents}{$doc};
    printf "    -- in document $doc VALUE=%.5f (%3.3s/%3.3s/%3.3s)\n", $doc_entity->{VALUE}, $doc_entity->{TYPE}, $doc_entity->{LEVEL}, $doc_entity->{CLASS};
    foreach my $mention (sort compare_locators @{$doc_entity->{mentions}}) {
      print "      mention TYPE=$mention->{TYPE}, ROLE=$mention->{ROLE}, STYLE=$mention->{STYLE}, ";
      printf "head=\"%s\", ", defined $mention->{head}{text} ? $mention->{head}{text} : "???";
      printf "extent=\"%s\"\n", defined $mention->{extent}{text} ? $mention->{extent}{text} : "???";
    }
    foreach my $name (sort compare_locators @{$doc_entity->{names}}) {
      printf "      name extent=\"%s\"\n", defined $name->{text} ? $name->{text} : "???";
    }
  }
}

#################################

sub print_relations {

  my ($db) = @_;

  foreach my $relation_id (sort keys %{$db->{relations}}) {
    my $relation = $db->{relations}{$relation_id};
    printf "relation ID=$relation->{ID}, VALUE=%.5f, TYPE=%s, SUBTYPE=%s",
    $relation->{VALUE}, $relation->{TYPE}, $relation->{SUBTYPE} ? $relation->{SUBTYPE} : "<none>";
    foreach my $attribute (@relation_attributes) {
      print ", $attribute=$relation->{$attribute}" unless
	$attribute =~ /^(ID|TYPE|SUBTYPE)$/ or not defined $relation->{$attribute};
    }
    print "\n";
    foreach my $role (sort keys %{$relation->{arguments}}) {
      print "  ".relation_argument_description ($relation->{arguments}{$role}, $db);
    }
    print_relation_mentions ($relation, $db->{refs});
  }
}

#################################

sub weighted_bipartite_graph_matching {
  my ($score) = @_;
    
  my $INF = 1E30;
  my (@row_mate, @col_mate, @row_dec, @col_inc);
  my (@parent_row, @unchosen_row, @slack_row, @slack);
  my ($k, $l, $row, $col, @col_min, $cost, %cost);
  my $t = 0;
    
  unless (defined $score) {
    warn "input to BGM is undefined\n";
    return undef;
  }
  return {} if (keys %$score) == 0;
    
  my @rows = sort keys %{$score};
  my $miss = "miss";
  $miss .= "0" while exists $score->{$miss};
  my (@cols, %cols);
  my $min_score = $INF;
  foreach $row (@rows) {
    foreach $col (keys %{$score->{$row}}) {
      $min_score = min($min_score,$score->{$row}{$col});
      $cols{$col} = $col;
    }
  }
  @cols = sort keys %cols;
  my $fa = "fa";
  $fa .= "0" while exists $cols{$fa};
  my $reverse_search = @rows < @cols; # search is faster when ncols <= nrows
  foreach $row (@rows) {
    foreach $col (keys %{$score->{$row}}) {
      ($reverse_search ? $cost{$col}{$row} : $cost{$row}{$col})
	= $score->{$row}{$col} - $min_score;
    }
  }
  push @rows, $miss;
  push @cols, $fa;
  if ($reverse_search) {
    my @xr = @rows;
    @rows = @cols;
    @cols = @xr;
  }

  my $nrows = @rows;
  my $ncols = @cols;
  my $nmax = max($nrows,$ncols);
  my $no_match_cost = -$min_score*(1+$required_precision);

  # subtract the column minimas
  for ($l=0; $l<$nmax; $l++) {
    $col_min[$l] = $no_match_cost;
    next unless $l < $ncols;
    $col = $cols[$l];
    foreach $row (keys %cost) {
      next unless defined $cost{$row}{$col};
      my $val = $cost{$row}{$col};
      $col_min[$l] = $val if $val < $col_min[$l];
    }
  }
    
  # initial stage
  for ($l=0; $l<$nmax; $l++) {
    $col_inc[$l] = 0;
    $slack[$l] = $INF;
  }
    
 ROW:
  for ($k=0; $k<$nmax; $k++) {
    $row = $k < $nrows ? $rows[$k] : undef;
    my $row_min = $no_match_cost;
    for (my $l=0; $l<$ncols; $l++) {
      my $col = $cols[$l];
      my $val = ((defined $row and defined $cost{$row}{$col}) ? $cost{$row}{$col}: $no_match_cost) - $col_min[$l];
      $row_min = $val if $val < $row_min;
    }
    $row_dec[$k] = $row_min;
    for ($l=0; $l<$nmax; $l++) {
      $col = $l < $ncols ? $cols[$l]: undef;
      $cost = ((defined $row and defined $col and defined $cost{$row}{$col}) ?
	       $cost{$row}{$col} : $no_match_cost) - $col_min[$l];
      if ($cost==$row_min and not defined $row_mate[$l]) {
	$col_mate[$k] = $l;
	$row_mate[$l] = $k;
	# matching row $k with column $l
	next ROW;
      }
    }
    $col_mate[$k] = -1;
    $unchosen_row[$t++] = $k;
  }
    
  goto CHECK_RESULT if $t == 0;
    
  my $s;
  my $unmatched = $t;
  # start stages to get the rest of the matching
  while (1) {
    my $q = 0;
	
    while (1) {
      while ($q < $t) {
	# explore node q of forest; if matching can be increased, update matching
	$k = $unchosen_row[$q];
	$row = $k < $nrows ? $rows[$k] : undef;
	$s = $row_dec[$k];
	for ($l=0; $l<$nmax; $l++) {
	  if ($slack[$l]>0) {
	    $col = $l < $ncols ? $cols[$l]: undef;
	    $cost = ((defined $row and defined $col and defined $cost{$row}{$col}) ?
		     $cost{$row}{$col} : $no_match_cost) - $col_min[$l];
	    my $del = $cost - $s + $col_inc[$l];
	    if ($del < $slack[$l]) {
	      if ($del == 0) {
		goto UPDATE_MATCHING unless defined $row_mate[$l];
		$slack[$l] = 0;
		$parent_row[$l] = $k;
		$unchosen_row[$t++] = $row_mate[$l];
	      } else {
		$slack[$l] = $del;
		$slack_row[$l] = $k;
	      }
	    }
	  }
	}
		
	$q++;
      }
	    
      # introduce a new zero into the matrix by modifying row_dec and col_inc
      # if the matching can be increased update matching
      $s = $INF;
      for ($l=0; $l<$nmax; $l++) {
	if ($slack[$l] and ($slack[$l]<$s)) {
	  $s = $slack[$l];
	}
      }
      for ($q = 0; $q<$t; $q++) {
	$row_dec[$unchosen_row[$q]] += $s;
      }
	    
      for ($l=0; $l<$nmax; $l++) {
	if ($slack[$l]) {
	  $slack[$l] -= $s;
	  if ($slack[$l]==0) {
	    # look at a new zero and update matching with col_inc uptodate if there's a breakthrough
	    $k = $slack_row[$l];
	    unless (defined $row_mate[$l]) {
	      for (my $j=$l+1; $j<$nmax; $j++) {
		if ($slack[$j]==0) {
		  $col_inc[$j] += $s;
		}
	      }
	      goto UPDATE_MATCHING;
	    } else {
	      $parent_row[$l] = $k;
	      $unchosen_row[$t++] = $row_mate[$l];
	    }
	  }
	} else {
	  $col_inc[$l] += $s;
	}
      }
    }
	
   UPDATE_MATCHING:		# update the matching by pairing row k with column l
    while (1) {
      my $j = $col_mate[$k];
      $col_mate[$k] = $l;
      $row_mate[$l] = $k;
      # matching row $k with column $l
      last UPDATE_MATCHING if $j < 0;
      $k = $parent_row[$j];
      $l = $j;
    }
	
    $unmatched--;
    goto CHECK_RESULT if $unmatched == 0;
	
    $t = 0;			# get ready for another stage
    for ($l=0; $l<$nmax; $l++) {
      $parent_row[$l] = -1;
      $slack[$l] = $INF;
    }
    for ($k=0; $k<$nmax; $k++) {
      $unchosen_row[$t++] = $k if $col_mate[$k] < 0;
    }
  }				# next stage

 CHECK_RESULT:			# rigorously check results before handing them back
  for ($k=0; $k<$nmax; $k++) {
    $row = $k < $nrows ? $rows[$k] : undef;
    for ($l=0; $l<$nmax; $l++) {
      $col = $l < $ncols ? $cols[$l]: undef;
      $cost = ((defined $row and defined $col and defined $cost{$row}{$col}) ?
	       $cost{$row}{$col} : $no_match_cost) - $col_min[$l];
      if ($cost < ($row_dec[$k] - $col_inc[$l])) {
	next unless $cost < ($row_dec[$k] - $col_inc[$l]) - $required_precision*max(abs($row_dec[$k]),abs($col_inc[$l]));
	warn "BGM: this cannot happen: cost{$row}{$col} ($cost) cannot be less than row_dec{$row} ($row_dec[$k]) - col_inc{$col} ($col_inc[$l])\n";
	return undef;
      }
    }
  }

  for ($k=0; $k<$nmax; $k++) {
    $row = $k < $nrows ? $rows[$k] : undef;
    $l = $col_mate[$k];
    $col = $l < $ncols ? $cols[$l]: undef;
    $cost = ((defined $row and defined $col and defined $cost{$row}{$col}) ?
	     $cost{$row}{$col} : $no_match_cost) - $col_min[$l];
    if (($l<0) or ($cost != ($row_dec[$k] - $col_inc[$l]))) {
      next unless $l<0 or abs($cost - ($row_dec[$k] - $col_inc[$l])) > $required_precision*max(abs($row_dec[$k]),abs($col_inc[$l]));
      warn "BGM: every row should have a column mate: row $row doesn't, col: $col\n";
      return undef;
    }
  }

  my %map;
  for ($l=0; $l<@row_mate; $l++) {
    $k = $row_mate[$l];
    $row = $k < $nrows ? $rows[$k] : undef;
    $col = $l < $ncols ? $cols[$l]: undef;
    next unless defined $row and defined $col and defined $cost{$row}{$col};
    $reverse_search ? ($map{$col} = $row) : ($map{$row} = $col);
  }
  return {%map};
}
