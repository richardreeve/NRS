# Copyright (C) 2006 Richard Reeve, Darren Smith, Matthew Szenher
#                    and Edinburgh University
#
#    This program is free software; you can redistribute it and/or
#    modify it under the terms of the GNU General Public License as
#    published by the Free Software Foundation; either version 2 of
#    the License, or (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public
#    License along with this program; if not, write to the Free
#    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
#    MA 02111-1307 USA
#
# For further information in the first instance contact:
# Richard Reeve <richardr@inf.ed.ac.uk>

dnl Process this file with autoconf to produce a configure script.
AC_INIT(nrs, 2.0-alpha)
AC_CONFIG_SRCDIR(doc/NRS.tex)
AC_CANONICAL_TARGET
AM_INIT_AUTOMAKE(subdir-objects)

AC_REVISION($Revision: 1.93 $)
AC_PREREQ(2.5)

AM_CONFIG_HEADER(include/config.h)

AC_LANG(C)

dnl Checks for programs.
export CFLAGS="-Wall -g -O2 -I/usr/local/include"
export LDFLAGS="-L/usr/local/lib"
AC_PROG_CC
AC_PROG_CXX
AC_PROG_MAKE_SET
AC_PROG_INSTALL
AC_CONFIG_MACRO_DIRS(m4)
LT_INIT

STD_CPPFLAGS="-I/usr/local/include"
AC_SUBST(STD_CPPFLAGS)

AC_ARG_WITH(koala,
[  --with-koala	Configure for koala build],
koala=true,
koala=false)
dnl AM_CONDITIONAL(KOALA, test x$koala = xtrue)

AC_ARG_WITH(khepera,
[  --with-khepera	Configure for khepera build],
khepera=true,
khepera=false)
dnl AM_CONDITIONAL(KHEPERA, test x$khepera = xtrue)

AC_ARG_WITH(khepera2,
[  --with-khepera2	Configure for khepera2 build],
khepera2=true,
khepera2=false)
dnl AM_CONDITIONAL(KHEPERA2, test x$khepera2 = xtrue)

AC_ARG_ENABLE(java,
[  --enable-java		Configure to build java code],
if test x$enableval = xyes; then
 java=true
else
 java=false
fi, java=true)
dnl AM_CONDITIONAL(JAVA_ACTIVE, test x$java = xtrue)

AC_ARG_ENABLE(debug,
[  --enable-debug	Configure to enable debugging],
if test x$enableval = xyes; then debug=true; else debug=false; fi,
debug=true)
if test $debug = true; then
  AC_DEFINE(DEBUG, 1, We are using debugging: _BUG_ and _TODO_ will warn)
fi
dnl AM_CONDITIONAL(DEBUG_ACTIVE, test x$debug = xtrue)

AC_ARG_ENABLE(camera,
[  --enable-camera	Configure to build camera libraries and camera tracker],
if test x$enableval = xyes; then
 camera=true
else
 camera=false
fi,
camera=false)

AC_ARG_ENABLE(audiosim,
[  --enable-audiosim	Configure to build cricket audio simulator],
if test x$enableval = xyes; then
 audiosim=true
else
 audiosim=false
fi,
audiosim=false)

AM_CONDITIONAL(AUDIOSIM_ACTIVE, test x$audiosim = xtrue)

AC_ARG_ENABLE(c-code,
[  --enable-c-code	Configure to do any C or C++ building except documentation],
if test x$enableval = xyes; then code=true; else code=false; fi,
code=true)

AC_ARG_ENABLE(docs,
[  --enable-docs		Configure to produce written documentation],
if test x$enableval = xyes; then
 docs=true
else
 docs=false
fi, docs=true)
AM_CONDITIONAL(DOCS_ACTIVE, test x$docs = xtrue)

AC_ARG_ENABLE(javadoc,
[  --enable-javadoc	Configure to use javadoc],
if test x$enableval = xyes; then
 javadoc=true
else
 javadoc=false
fi,
javadoc=unknown)
AM_CONDITIONAL(JAVADOC_ACTIVE, test x$javadoc = xtrue)

AC_ARG_ENABLE(doxygen,
[  --enable-doxygen	Configure to use doxygen],
if test x$enableval = xyes; then
 doxygen=true
else
 doxygen=false
fi, doxygen=unknown)
AM_CONDITIONAL(DOXYGEN_ACTIVE, test x$doxygen = xtrue)


dnl These are the directories which have to be recompiled for every target
export SUBDIRS=

AC_DEFINE_UNQUOTED(PREFIX, "$prefix",
		    The prefix for the installation directory)

if test x$host = xavr-unknown-none; then
  dnl Writing code for AVR microcontrollers - add any avr subdirectories here
  # Output format. (can be srec, ihex, binary)
  export FORMAT=ihex
  # atmel processor
  export MCU=atmega128
  AC_PATH_PROG(AVROBJCOPY, avr-objcopy)
  AC_PATH_PROG(AVROBJDUMP, avr-objdump)
  AC_DEFINE(__MICRO_avr, 1, We are compiling for an avr microcontroller)
  export SUBDIRS="$SUBDIRS robot robot/common_csl robot/common_csl/message robot/atmel_csl robot/atmel_csl/message"
  export CFLAGS="-D__MICRO_avr -Wall -g -Os -funsigned-char -funsigned-bitfields -fpack-struct -fshort-enums -std=gnu99 -mmcu=@MCU@"
#  export AVRLDFLAGS="`$KTEAM_CONFIG --$AVRDIR --ldflags`"
  export AVROBJCOPYFLAGS="-O @FORMAT@ -R .eeprom"
#  AC_SUBST(AVRLDFLAGS)
  AC_SUBST(FORMAT)
  AC_SUBST(MCU)
  AC_SUBST(AVROBJCOPY)
  AC_SUBST(AVROBJDUMP)
  AC_SUBST(AVROBJCOPYFLAGS)
  export AVREXE="component.hex"
  export AVRBUILD="component.hex component.elf"
  AC_SUBST(AVREXE)
  AC_SUBST(AVRBUILD)
else
 if test x$host = xm68k-none-elf; then
  export ROBOTEXE="main.srec"
  export ROBOTBUILD="main.srec main.elf"
  AC_SUBST(ROBOTEXE)
  AC_SUBST(ROBOTBUILD)
  AC_PATH_PROG(KTEAM_CONFIG, kteam-config)
  export KTEAM_CONFIG
  # add k-team specific subdirectories here
  if test x$code = xtrue; then
    export SUBDIRS="$SUBDIRS robot robot/kh2_csl robot/kh2_csl/message robot/common_csl robot/common_csl/message"
  fi
  AC_DEFINE(__ROBOT_kteam, 1, We are compiling for a k-team platform)
  AC_MSG_CHECKING(robot target chosen)
  if test x$koala = xtrue; then
   dnl Host is Koala - add koala specific subdirectories here
   export SUBDIRS="$SUBDIRS"
   export ROBOTDIR="koala"

   AC_DEFINE(__ROBOT_koa, 1, We are compiling for a Koala robot)
   if test x$khepera = xtrue; then
     AC_MSG_ERROR(We cannot target multiple robots simultaneously)
   fi
   if test x$khepera2 = xtrue; then
     AC_MSG_ERROR(We cannot target to robots simultaneously)
   fi

   AC_MSG_RESULT(koala)
  else
   if test x$khepera = xtrue; then
     dnl Host is khepera - add khepera specific subdirectories here
     export SUBDIRS="$SUBDIRS"
     export ROBOTDIR="khepera"

     AC_DEFINE(__ROBOT_khe, 1, We are compiling for a Khepera robot)
     if test x$khepera2 = xtrue; then
       AC_MSG_ERROR(We cannot target multiple robots simultaneously)
     fi

     AC_MSG_RESULT(khepera)
   else
     if test x$khepera2 = xtrue; then
       dnl Host is khepera 2 - add khe2 specific subdirectories here
       export SUBDIRS="$SUBDIRS"
       export ROBOTDIR="khepera2"

       AC_DEFINE(__ROBOT_kh2, 1, We are compiling for a Khepera2 robot)

       AC_MSG_RESULT(khepera2)
     else
       AC_MSG_ERROR(We have to target exactly 1 robot)
     fi
   fi
  fi
  AC_SUBST(ROBOTDIR)
  export CFLAGS="$CFLAGS `$KTEAM_CONFIG --$ROBOTDIR --cflags`"
  export ROBOTLIBS="`$KTEAM_CONFIG --$ROBOTDIR --libs`"
  export ROBOTARFLAGS="`$KTEAM_CONFIG --$ROBOTDIR --arflags`"
  export ROBOTLDFLAGS="`$KTEAM_CONFIG --$ROBOTDIR --ldflags`"
  export ROBOTOBJCOPY="`$KTEAM_CONFIG --$ROBOTDIR --objcopy`"
  export ROBOTOBJDUMP="`$KTEAM_CONFIG --$ROBOTDIR --objdump`"
  export ROBOTOBJCOPYFLAGS="`$KTEAM_CONFIG --$ROBOTDIR --objcopyflags`"
  export ROBOTOBJDUMPFLAGS="`$KTEAM_CONFIG --$ROBOTDIR --objdumpflags`"
  AC_SUBST(ROBOTLIBS)
  AC_SUBST(ROBOTARFLAGS)
  AC_SUBST(ROBOTLDFLAGS)
  AC_SUBST(ROBOTOBJCOPY)
  AC_SUBST(ROBOTOBJDUMP)
  AC_SUBST(ROBOTOBJCOPYFLAGS)
  AC_SUBST(ROBOTOBJDUMPFLAGS)
 else
   dnl host is windows under cygwin, linux or macosx
   dnl Hence a full build is needed - add all main subdirectories here

   if test x$code = xtrue; then
     # INSERT: add new component directories after NSim
     export SUBDIRS="$SUBDIRS Base Type Unit Message NSim audiogen PML BMF tracker component download socket"
     AC_LIBTOOL_DLOPEN
   fi

   if test x$koala = xtrue; then
     AC_MSG_ERROR(host=m68k-none-elf must be set to target a robot)
   fi
   if test x$khepera = xtrue; then
     AC_MSG_ERROR(host=m68k-none-elf must be set to target a robot)
   fi
   if test x$khepera2 = xtrue; then
     AC_MSG_ERROR(host=m68k-none-elf must be set to target a robot)
   fi

   if test x$code = xtrue; then
     AC_C_INLINE
     AC_C_CONST
     export CXXFLAGS="-Wall -g -O2"
     AC_PROG_CXX
     AC_CHECK_LIB(dl, dlopen, dlpresent=true, dlpresent=false)
     if test x$dlpresent = xfalse; then
       AC_MSG_ERROR(Must have dl library present for plugins to work)
     fi

     AC_LANG_PUSH(C++)
     AC_CHECK_LIB(xerces-c, main, xercespresent=true, xercespresent=false)
     if test x$xercespresent = xfalse; then
       AC_MSG_ERROR(Must have xerces library present for xml to work)
     fi
     AC_LANG_POP(C++)

     export KERNELDIR="/lib/modules/`uname -r`/build"
     AC_SUBST(KERNELDIR)

     export DLOPEN=-dlopen
     AC_SUBST(DLOPEN)
     AC_CHECK_HEADERS(ltdl.h, ltdlpresent=true, ltdlpresent=false)
     if test x$ltdlpresent = xfalse; then
       AC_MSG_ERROR([Must have header file ltdl.h for C++ libraries/programs to compile])
     fi

     AC_CHECK_SIZEOF(short)
     AC_CHECK_SIZEOF(int)
     AC_CHECK_SIZEOF(long)
     AC_CHECK_SIZEOF(long long)

     AC_CHECK_SIZEOF(float)
     AC_CHECK_SIZEOF(double)

     AC_CHECK_TYPES([ uint8_t, int8_t, uint16_t, int16_t,
		       uint32_t, int32_t, uint64_t, int64_t ])

     if test "$ac_cv_sizeof_float" != "4" ; then
       AC_MSG_ERROR(float length must be 4 bytes, sorry);
     fi

     if test "$ac_cv_sizeof_double" != "8" ; then
       AC_MSG_ERROR(double length must be 8 bytes, sorry);
     fi

     if test "${ac_cv_sizeof_short}" != "2"; then
       AC_MSG_ERROR(We have no 2 byte types - do we need them?);
     fi

     if test "${ac_cv_sizeof_int}" != "4"; then
       AC_MSG_ERROR(Can't find a 4 byte type)
     fi

     if test "${ac_cv_sizeof_long_long}" != "8"; then
       AC_MSG_ERROR(Can't find an 8 byte type)
     fi

     AH_VERBATIM([HAVE_INT8_T],
[/* add int8_t type */
#ifndef __MICRO_avr
#undef HAVE_INT8_T
#ifndef HAVE_INT8_T
	typedef char int8_t;
#endif
#endif])

     AH_VERBATIM([HAVE_UINT8_T],
[/* add uint8_t type */
#ifndef __MICRO_avr
#undef HAVE_UINT8_T
#ifndef HAVE_UINT8_T
	typedef unsigned char uint8_t;
#endif
#endif])

     AH_VERBATIM([HAVE_INT16_T],
[/* add int16_t type */
#ifndef __MICRO_avr
#undef HAVE_INT16_T
#ifndef HAVE_INT16_T
	typedef short int16_t;
#endif
#endif])

     AH_VERBATIM([HAVE_UINT16_T],
[/* add uint16_t type */
#ifndef __MICRO_avr
#undef HAVE_UINT16_T
#ifndef HAVE_UINT16_T
	typedef unsigned short uint16_t;
#endif
#endif])

     AH_VERBATIM([HAVE_INT32_T],
[/* add int32_t type */
#ifndef __MICRO_avr
#undef HAVE_INT32_T
#ifndef HAVE_INT32_T
	typedef int int32_t;
#endif
#endif])

     AH_VERBATIM([HAVE_UINT32_T],
[/* add uint32_t type */
#ifndef __MICRO_avr
#undef HAVE_UINT32_T
#ifndef HAVE_UINT32_T
	typedef unsigned int uint32_t;
#endif
#endif])

     AH_VERBATIM([HAVE_INT64_T],
[/* add int64_t type */
#ifndef __MICRO_avr
#undef HAVE_INT64_T
#ifndef HAVE_INT64_T
	typedef long long int64_t;
#endif
#endif])

     AH_VERBATIM([HAVE_UINT64_T],
[/* add uint64_t type */
#ifndef __MICRO_avr
#undef HAVE_UINT64_T
#ifndef HAVE_UINT64_T
	typedef unsigned long long uint64_t;
#endif
#endif])

     if test x$camera = xtrue; then
       AC_CHECK_HEADERS(opencv/cv.h,
                        openCVheaderpresent=true,
                        openCVheaderpresent=false)

        if test x$openCVheaderpresent = xfalse; then
           AC_CHECK_HEADERS(${OPENCV_INCDIR}/opencv/cv.h,
                            openCVheaderpresent=true,
                            openCVheaderpresent=false)
           if test x$openCVheaderpresent = xfalse; then
              AC_MSG_ERROR([Must have header file opencv/cv.h for camera library/programs to compile])
           fi
        fi

        AC_CHECK_HEADERS(opencv/highgui.h,
                         openCVhighguiheaderpresent=true,
                         openCVhighguiheaderpresent=false)

        if test x$openCVhighguiheaderpresent = xfalse; then
           AC_CHECK_HEADERS(${OPENCV_INCDIR}/opencv/highgui.h,
                            openCVhighguiheaderpresent=true,
                            openCVhighguiheaderpresent=false)
           if test x$openCVhighguiheaderpresent = xfalse; then
              AC_MSG_ERROR([Must have header file opencv/highgui.h for camera library/programs to compile])
           fi
        fi

        AC_CHECK_HEADERS(ccvt.h,
                         ccvtpresent=true,
                         ccvtpresent=false)

        if test x$ccvtpresent = xfalse; then
	   AC_CHECK_HEADERS(${CCVT_INCDIR}/ccvt.h,
                        ccvtpresent=true,
                        ccvtpresent=false)
           if test x$ccvtpresent = xfalse; then
               AC_MSG_ERROR([Must have header file ccvt.h for camera library/programs to compile])
           fi
        fi

        AC_LANG_PUSH(C++)
        AC_CHECK_LIB(opencv, cvCreateImage, lopencvok=true, lopencvok=false)
        if test x$lopencvok = xfalse; then
	   if test x${OPENCV_LIBDIR} = x; then
              AC_MSG_ERROR([Must have opencv library for camera library/programs to build])
           fi
	fi

        AC_CHECK_LIB(highgui, cvShowImage, lhighguiok=true, lhighguiok=false,
                     [-lopencv])
        if test x$lhighguiok = xfalse; then
	   if test x${OPENCV_LIBDIR} = x; then
              AC_MSG_ERROR([Must have highgui library (OpenCV) for camera library/programs to build])
           fi
        fi
        AC_LANG_POP(C++)
	if test x${OPENCV_INCDIR} = x; then
	   export OPENCV_INCLUDE=
	else
	   export OPENCV_INCLUDE=-I${OPENCV_INCDIR}
	fi
	if test x${OPENCV_LIBDIR} = x; then
	   export OPENCV_LIBRARY=
	else
	   export OPENCV_LIBRARY="-L${OPENCV_LIBDIR} -R${OPENCV_LIBDIR}"
	fi
   	AC_SUBST(OPENCV_INCLUDE)
   	AC_SUBST(OPENCV_LIBRARY)

        AC_CHECK_LIB(ccvt, ccvt_420p_bgr24, lccvtok=true, lccvtok=false)
        if test x$lccvtok = xfalse; then
	   if test x${CCVT_LIBDIR} = x; then
              AC_MSG_ERROR([Must have ccvt library for camera library/programs to build])
           fi
        fi
	if test x${CCVT_INCDIR} = x; then
	   export CCVT_INCLUDE=
	else
	   export CCVT_INCLUDE=-I${CCVT_INCDIR}
	fi
	if test x${CCVT_LIBDIR} = x; then
	   export CCVT_LIBRARY=
	else
	   export CCVT_LIBRARY="-L${CCVT_LIBDIR} -R${CCVT_LIBDIR}"
	fi
   	AC_SUBST(CCVT_INCLUDE)
   	AC_SUBST(CCVT_LIBRARY)

        export SUBDIRS="$SUBDIRS camera camera_tracker"
      fi
    fi

    if test x$docs = xtrue; then
      export SUBDIRS="$SUBDIRS doc"
      AC_PATH_PROG(PDFLATEX, pdflatex, missing)
      if test x$PDFLATEX = xmissing; then
        AC_MSG_ERROR(Must have pdflatex present to build documentation)
      fi

      AC_PATH_PROG(BIBTEX, bibtex, missing)
      if test x$BIBTEX = xmissing; then
        AC_MSG_ERROR(Must have bibtex present to build documentation)
      fi

      AC_PATH_PROG(EPSTOPDF, epstopdf, missing)
      if test x$EPSTOPDF = xmissing; then
        AC_MSG_ERROR(Must have epstopdf present to build documentation)
      fi

      AC_PATH_PROG(DIA, dia-cli, missing)
      if test x$DIA = xmissing; then
        AC_PATH_PROG(DIA, dia, missing)
        if test x$DIA = xmissing; then
          AC_MSG_ERROR(Must have dia present to build documentation)
        fi
      fi
      AC_SUBST(PDFLATEX)
      AC_SUBST(BIBTEX)
      AC_SUBST(EPSTOPDF)
      AC_SUBST(DIA)
    fi

    if test x$doxygen = xtrue; then
      AC_PATH_PROG(DOXYGEN, doxygen)
      if test x$DOXYGEN = xmissing; then
        AC_MSG_ERROR(Must have doxygen present to build API documentation)
      fi
      AC_SUBST(DOXYGEN)
      export SUBDIRS="$SUBDIRS doxygen"
    fi

    if test x$java = xtrue; then
      export SUBDIRS="$SUBDIRS nrs icons"

      javaDICE="/usr/lib/jvm/java-1.5.0-sun-1.5.0/bin"
      javaDJS="/usr/java/jdk1.5.0_01/bin"

      javadirs=
      for tmpdir in /System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands ${JAVA_HOME}/bin; do if test -d $tmpdir; then javadirs="$javadirs$tmpdir:"; fi; done
      AC_PATH_PROG(JAVAC, javac, failed, ${javadirs}/)
      if test x$JAVAC = xfailed;
      then
        AC_PATH_PROG(JAVAC, javac)
        if test x$JAVAC = xfailed; then
          AC_MSG_ERROR(Must have javac present to build java code)
        fi
      fi
      export JAVA_SCRIPTS="NRS.gui NRS.RobotTrackerGUI NRS.StickTrackerGUI NRS.control NRS.ga NRS.datalogger NRS.composer NRS.wrapper NRS.calculation NRS.sound NRS.oscilloscope"
      if test x$audiosim = xtrue; then
        export JAVA_SCRIPTS="$JAVA_SCRIPTS NRS.audiosim"
      fi

      AC_PATH_PROG(JAVAD, javadoc, failed, ${javadirs}/)
      if test x$JAVAD = xfailed;
      then
        AC_PATH_PROG(JAVAD, javadoc)
        if test x$JAVAD = xfailed; then
          AC_MSG_ERROR(Must have javadoc present to build java documenation)
        fi
      fi

      AC_PATH_PROG(JAVA, java, failed, ${javadirs}/)
      if test x$JAVA = xfailed;
      then
        AC_PATH_PROG(JAVA, java)
        if test x$JAVA = xmissing; then
          AC_MSG_ERROR(Must have java present to run java code)
        fi
      fi

      AC_SUBST(JAVAC)
      AC_SUBST(JAVAD)
      AC_SUBST(JAVA)

      AC_MSG_CHECKING(for Xerces archive)
      if test x${CLASSPATH} != x && test x`echo ${CLASSPATH} | grep xerces` != x; then
        CLASSPATH=${CLASSPATH}
          AC_MSG_RESULT(${CLASSPATH})
      else
        if test x${XERCES} != x; then
          if test x${CLASSPATH} != x; then
            CLASSPATH=${CLASSPATH}:${XERCES}
          else
            CLASSPATH=${XERCES}
          fi
          AC_MSG_RESULT(${XERCES})
        else
          AC_MSG_RESULT(checking for individual files)
          tmpdirs=
          for tmpdir in ~/software/xerces /sw/share/java/xerces-j /usr/share/java /usr/java/lib/ant-1.5.1/lib /usr/local/share/xerces-2_5_0 /usr/local/external/xerces-2_5_0; do if test -d $tmpdir; then tmpdirs="$tmpdirs$tmpdir:"; fi; done
          AC_PATH_PROGS(XERCES1, xml-apis.jar xml-commons-apis.jar, failed, ${tmpdirs}/)
          if test ${XERCES1} = failed; then
            AC_MSG_ERROR(Could not find Xerces archive file containing all the standard API's implemented by the parser. Please provide in CLASSPATH or XERCES environment variable or add directory to tests in configure.ac.)
          else
            if test x${CLASSPATH} != x; then
              CLASSPATH=${CLASSPATH}:${XERCES1}
            else
              CLASSPATH=${XERCES1}
            fi
          fi
        AC_PATH_PROGS(XERCES2, xerces-j2.jar xercesImpl.jar, failed, ${tmpdirs}/)
          if test ${XERCES2} = failed; then
            AC_MSG_ERROR(Could not find Xerces archive: please provide in CLASSPATH or XERCES environment variable or add directory to tests in configure.ac.)
          else
            CLASSPATH=${CLASSPATH}:${XERCES2}
          fi
        fi
      fi
      if test x$audiosim = xtrue; then
       AC_MSG_CHECKING(for batik SVG archive)
       if test x${CLASSPATH} != x && test x`echo ${CLASSPATH} | grep batik` != x; then
        CLASSPATH=${CLASSPATH}
        AC_MSG_RESULT(${CLASSPATH})
       else
        if test x${BATIK} != x; then
          if test x${CLASSPATH} != x; then
            CLASSPATH=${CLASSPATH}:${BATIK}
          else
            CLASSPATH=${BATIK}
          fi
          AC_MSG_RESULT(${BATIK})
        else
          AC_MSG_RESULT(checking for individual files)
          tmpdirs=
          for tmpdir in /usr/share/java /usr/share/java/batik; do if test -d $tmpdir; then tmpdirs="$tmpdirs$tmpdir:"; fi; done
          AC_PATH_PROGS(SVG1, batik.jar svggen.jar, failed, ${tmpdirs}/)
          if test ${SVG1} = failed; then
            AC_MSG_ERROR(Could not find batik archive file containing all the standard APIs. Please provide in CLASSPATH or BATIK environment variable or add directory to tests in configure.ac.)
          else
            if test x${CLASSPATH} != x; then
              CLASSPATH=${CLASSPATH}:${SVG1}
            else
              CLASSPATH=${SVG1}
            fi
          fi
          AC_PATH_PROGS(SVG2, batik.jar util.jar, failed, ${tmpdirs}/)
          if test ${SVG2} = failed; then
            AC_MSG_ERROR(Could not find batik archive file containing all the standard APIs. Please provide in CLASSPATH or BATIK environment variable or add directory to tests in configure.ac.)
          else
            CLASSPATH=${CLASSPATH}:${SVG2}
          fi
          AC_PATH_PROGS(SVG3, batik.jar awt-util.jar, failed, ${tmpdirs}/)
          if test ${SVG3} = failed; then
            AC_MSG_ERROR(Could not find batik archive file containing all the standard APIs. Please provide in CLASSPATH or BATIK environment variable or add directory to tests in configure.ac.)
          else
            CLASSPATH=${CLASSPATH}:${SVG3}
          fi
        fi
       fi
      fi
      CLASSPATH=${CLASSPATH}:.
      AC_SUBST(CLASSPATH)

      export JAVA_GUI_PATH=.
      AC_SUBST(JAVA_GUI_PATH)
    fi
  fi
fi
AC_PROG_LIBTOOL
AC_SUBST(SUBDIRS)
AC_SUBST(JAVA_SCRIPTS)

dnl Checks for libraries.

dnl Checks for header files.

dnl Checks for typedefs, structures, and compiler characteristics.

dnl Checks for library functions.

# INSERT: add path to Makefile for new component after NSim/Makefile
AC_OUTPUT(Makefile Base/Makefile Type/Makefile \
	Unit/Makefile NSim/Makefile tracker/Makefile camera/Makefile \
	component/Makefile doc/Makefile nrs/Makefile \
	icons/Makefile icons/nuvola/Makefile \
	icons/nuvola/16x16/Makefile icons/nuvola/22x22/Makefile \
	PML/Makefile BMF/Makefile Message/Makefile \
	nrs/csl/Makefile nrs/toolboxes/Makefile \
	nrs/toolboxes/multitable/Makefile \
	nrs/nrsgui/Makefile doxygen/Makefile \
        nrs/tracker/Makefile nrs/tracker/palette/Makefile \
	nrs/tracker/robottrack/Makefile nrs/tracker/jointmanager/Makefile \
	robot/Makefile download/Makefile nrs/tracker/stickgui/Makefile \
        nrs/tracker/sticktrack/Makefile nrs/pml/Makefile \
        camera_tracker/Makefile nrs/control/Makefile \
        nrs/core/Makefile nrs/core/base/Makefile nrs/core/comms/Makefile \
        nrs/core/message/Makefile nrs/audio/Makefile
        nrs/audio/simulator/Makefile audiogen/Makefile \
	nrs/ga/Makefile nrs/ga/base/Makefile \
	nrs/ga/mutation/Makefile nrs/ga/crossover/Makefile \
	nrs/ga/selection/Makefile nrs/ga/population/Makefile \
	nrs/ga/rng/Makefile nrs/ga/replacement/Makefile \
	nrs/util/Makefile nrs/util/job/Makefile nrs/datalogger/Makefile \
	nrs/composer/Makefile nrs/core/base/message/Makefile \
	nrs/core/base/discovery/Makefile nrs/wrapper/Makefile \
        nrs/calculation/Makefile socket/Makefile \
        nrs/core/type/Makefile nrs/core/unit/Makefile nrs/sound/Makefile \
	nrs/oscilloscope/Makefile nrs/oscilloscope/nodes/Makefile \
	nrs/oscilloscope/oscilloscopeGUI/Makefile \
	robot/common_csl/Makefile robot/common_csl/message/Makefile \
	robot/atmel_csl/Makefile robot/atmel_csl/message/Makefile \
	robot/kh2_csl/Makefile robot/kh2_csl/message/Makefile)
