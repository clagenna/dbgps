package sm.clagenna.dbgps.cmdline;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

public class RigaComando {
  private static final Logger s_log = LogManager.getLogger(RigaComando.class);

  public static final String CSZ_OPT_DSTDB     = "dstDB";
  public static final String CSZ_OPT_DSTGPX    = "dstGPX";
  public static final String CSZ_OPT_SRCFILE   = "src";
  public static final String CSZ_OPT_OVERWRITE = "over";

  private static RigaComando s_inst;

  private Options     opts;
  private CommandLine cmdl;

  @Getter
  private Path    sourceFile;
  @Getter
  private Path    destDB;
  @Getter
  private Path    destGPX;
  @Getter
  private boolean overwrite;

  public RigaComando() {
    s_inst = this;
    creaOptions();
  }

  public static RigaComando getInst() {
    return s_inst;
  }

  public void creaOptions() {
    opts = new Options();
    final boolean WITH_ARGS = true;
    final boolean NO_ARGS = false;
    Option op = new Option(CSZ_OPT_SRCFILE, WITH_ARGS, "il file JSON sorgente da convertire");
    op.setRequired(true);
    opts.addOption(op);

    op = new Option(CSZ_OPT_DSTDB, WITH_ARGS, "Il DB sqlite di destinazione dei GPS");
    opts.addOption(op);

    op = new Option(CSZ_OPT_DSTGPX, WITH_ARGS, "Il GPX di destinazione dei GPS");
    opts.addOption(op);

    op = new Option(CSZ_OPT_OVERWRITE, NO_ARGS, "Se sovrascrivere il DB di Destinazione");
    opts.addOption(op);

  }

  public boolean parseOption(String[] args) {
    boolean bRet = false;
    destDB = null;
    destGPX = null;
    CommandLineParser prs = new DefaultParser();
    if (args.length == 0) {
      help();
      return bRet;
    }

    try {
      cmdl = prs.parse(opts, args);
      bRet = controllaOptions();
    } catch (ParseException e) {
      s_log.error(e.getMessage());
      help();
    }
    return bRet;
  }

  public boolean isGpxNeeded() {
    return destGPX != null;
  }

  public boolean isDBNeeded() {
    return destDB != null;
  }

  private boolean controllaOptions() throws ParseException {
    if ( !cmdl.hasOption(CSZ_OPT_SRCFILE))
      throw new ParseException("Non hai specificato il file sorgente");
    sourceFile = Paths.get(cmdl.getOptionValue(CSZ_OPT_SRCFILE));
    if ( !Files.exists(sourceFile, LinkOption.NOFOLLOW_LINKS))
      throw new ParseException("Non esiste il file sorgente");
    //    if ( !cmdl.hasOption(CSZ_OPT_DSTDB))
    //      throw new ParseException("Non hai specificato il DB di destinazione");
    overwrite = cmdl.hasOption(CSZ_OPT_OVERWRITE);
    if (cmdl.hasOption(CSZ_OPT_DSTDB)) {
      destDB = Paths.get(cmdl.getOptionValue(CSZ_OPT_DSTDB));
      if (Files.exists(destDB, LinkOption.NOFOLLOW_LINKS) && !overwrite)
        throw new ParseException("Il DB di destinazione esiste gia'");
    }
    if (cmdl.hasOption(CSZ_OPT_DSTGPX)) {
      destGPX = Paths.get(cmdl.getOptionValue(CSZ_OPT_DSTGPX));
      if (Files.exists(destGPX, LinkOption.NOFOLLOW_LINKS) && !overwrite)
        throw new ParseException("Il GPX di destinazione esiste gia'");
    }
    if ( !( isDBNeeded() || isGpxNeeded())) {
      throw new ParseException("Devi specificare in alternativa o entrambi il DB o IL GPX di destinazione");
    }
    return true;
  }

  public void help() {
    HelpFormatter hlp = new HelpFormatter();
    hlp.printHelp("Converti GPS", opts, true);
  }

}
